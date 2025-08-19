import com.churchapp.entity.User
import com.churchapp.entity.enums.RoleType
import java.util.*

fun main() {
    // Test 1: Users with same ID should be equal
    val id = UUID.randomUUID()
    val user1 = User(id = id, username = "user1", password = "pass", email = "test1@example.com")
    val user2 = User(id = id, username = "user2", password = "pass", email = "test2@example.com")
    
    println("Test 1 - Same ID: ${user1 == user2} (should be true)")
    println("Test 1 - Same hashCode: ${user1.hashCode() == user2.hashCode()} (should be true)")
    
    // Test 2: Users with different IDs should not be equal
    val id2 = UUID.randomUUID()
    val user3 = User(id = id2, username = "user1", password = "pass", email = "test1@example.com")
    
    println("Test 2 - Different ID: ${user1 == user3} (should be false)")
    
    // Test 3: Transient users (no ID) should use reference equality
    val transient1 = User(username = "trans1", password = "pass", email = "trans1@example.com")
    val transient2 = User(username = "trans1", password = "pass", email = "trans1@example.com")
    
    println("Test 3 - Transient equality: ${transient1 == transient2} (should be false - reference equality)")
    println("Test 3 - Self equality: ${transient1 == transient1} (should be true)")
    
    // Test 4: Mutation doesn't affect equality/hashCode for persisted entities
    user1.setActive(false)
    println("Test 4 - After mutation: ${user1 == user2} (should still be true)")
    println("Test 4 - Same hashCode after mutation: ${user1.hashCode() == user2.hashCode()} (should still be true)")
}
