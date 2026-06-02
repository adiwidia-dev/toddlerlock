# Security Policy

ToddlerLock is intended to be a small Android utility app with no backend service and no required API keys.

## Reporting Issues

Please report security issues privately to the repository owner before opening a public issue.

## Secret Handling

Do not commit:

- `.env` files
- `local.properties`
- release keystores (`*.jks`, `*.keystore`)
- signing passwords
- API keys or service account files
- `google-services.json` unless a future Firebase integration explicitly requires a public-safe config

Release signing credentials must be supplied through local environment variables:

- `KEYSTORE_PATH`
- `STORE_PASSWORD`
- `KEY_PASSWORD`

## Public Repository Checklist

Before making the repository public:

- Run the tests and lint checks in `RELEASE.md`.
- Run the local secret scans in `RELEASE.md`.
- Confirm `.idea/`, build output, local SDK paths, and release keystores are not tracked.
