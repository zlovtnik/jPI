package com.churchapp.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.camel.Exchange
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.SimpleJdbcCall
import org.springframework.stereotype.Service
import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.Types

@Service
class DatabaseService(
    dataSource: DataSource
) {

    private val logger = LoggerFactory.getLogger(DatabaseService::class.java)
    private val jdbcTemplate = JdbcTemplate(dataSource)

    // RowMapper that converts a ResultSet row into a Map<String, Any?>
    private val rowToMap: RowMapper<Map<String, Any?>> = RowMapper { rs, _ ->
        val md = rs.metaData
        val row = mutableMapOf<String, Any?>()
        for (i in 1..md.columnCount) {
            row[md.getColumnLabel(i)] = rs.getObject(i)
        }
        row
    }

    // Per-procedure SimpleJdbcCall configurators. Use this to declare OUT params or returningResultSet.
    private val procedureConfigurators: Map<String, (SimpleJdbcCall) -> SimpleJdbcCall> = mapOf(
        // Example: procedures that return a result set / cursor
        "GetMemberDonationSummary" to { call -> call.returningResultSet("result", rowToMap) },
        "GetTopDonors" to { call -> call.returningResultSet("result", rowToMap) },
        "SearchMembers" to { call -> call.returningResultSet("result", rowToMap) }
        // Add more entries here for procedures with OUT params or special mappings
    )

    /**
     * Generic stored-procedure caller for Camel routes.
     * Expects header "procedureName" to contain the procedure name.
     * Maps the incoming body (Map or JSON string) and other headers to named parameters.
     * The result of the call is set as the exchange message body.
     */
    fun callProcedure(exchange: Exchange) {
        val message = exchange.message
        val procedureName = message.getHeader("procedureName", String::class.java)

        if (procedureName.isNullOrBlank()) {
            val err = "Missing procedureName header"
            logger.error(err)
            message.body = mapOf("error" to err)
            message.setHeader("procedureError", true)
            return
        }

        try {
            // Build parameter map from body and headers
            val paramMap = mutableMapOf<String, Any?>()

            // Extract body parameters
            val body = message.body
            if (body is Map<*, *>) {
                body.forEach { (k, v) -> if (k is String) paramMap[k] = v }
            } else if (body is String && body.trim().startsWith("{")) {
                try {
                    val mapper = jacksonObjectMapper()
                    val parsed: Map<String, Any?> = mapper.readValue(body)
                    paramMap.putAll(parsed)
                } catch (e: Exception) {
                    // treat as raw string if JSON parsing fails
                    paramMap["body"] = body
                }
            } else if (body != null) {
                paramMap["body"] = body
            }

            // Merge headers (but don't overwrite explicit body params)
            message.headers.forEach { (k, v) ->
                if (k != "procedureName" && !paramMap.containsKey(k)) {
                    paramMap[k] = v
                }
            }

            logger.debug("Calling procedure $procedureName with params=$paramMap")

            var simpleCall = SimpleJdbcCall(jdbcTemplate).withProcedureName(procedureName)

            // Apply per-procedure configurator if present (declare OUT params, return resultset, etc.)
            procedureConfigurators[procedureName]?.let { configurator ->
                simpleCall = configurator(simpleCall)
            }

            // Convert to Map for execution
            val paramSource = mutableMapOf<String, Any?>()
            paramMap.forEach { (k, v) -> paramSource[k] = v }

            val rawResult: Map<String, Any?> = simpleCall.execute(paramSource)

            // Normalize result: if any value is a ResultSet / Cursor-like, convert to list of maps
            val normalized = rawResult.mapValues { (_, v) ->
                when (v) {
                    is ResultSet -> {
                        // Convert ResultSet to list of maps
                        val rows = mutableListOf<Map<String, Any?>>()
                        val rs = v
                        val md = rs.metaData
                        while (rs.next()) {
                            val row = mutableMapOf<String, Any?>()
                            for (i in 1..md.columnCount) {
                                row[md.getColumnLabel(i)] = rs.getObject(i)
                            }
                            rows.add(row)
                        }
                        rows
                    }
                    is List<*> -> v
                    else -> v
                }
            }

            // Set the result as the outgoing body
            message.body = normalized
            message.setHeader("procedureError", false)
        } catch (e: Exception) {
            logger.error("Stored procedure call failed: ${'$'}{e.message}", e)
            message.body = mapOf("error" to (e.message ?: "unknown error"))
            message.setHeader("procedureError", true)
        }
    }
}
