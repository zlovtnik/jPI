# jpi Helm chart

This chart deploys the jPI application.

## Ingress `paths` format

The `ingress.hosts[].paths` entries support two forms for convenience:

1. Object form (recommended)

```yaml
ingress:
  enabled: true
  hosts:
    - host: example.com
      paths:
        - path: "/"
          pathType: Prefix
        - path: "/api"
          pathType: Prefix
```

This form allows you to explicitly set `pathType` (e.g. `Prefix` or `ImplementationSpecific`) and is the recommended format for clarity.

2. String form (backwards-compatible)

```yaml
ingress:
  enabled: true
  hosts:
    - host: example.com
      paths:
        - "/"
        - "/api"
```

When a path is provided as a plain string, the chart treats it as `path: "<string>"` with `pathType: "Prefix"`.

Use the object form for new charts and when you need to specify a `pathType` other than `Prefix`.
