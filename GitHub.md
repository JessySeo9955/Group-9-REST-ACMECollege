# GitHub Authentication (Personal Access Token)

GitHub no longer allows authentication using your GitHub account password when pushing through Eclipse. Instead, you must use a **Personal Access Token (PAT)**.

---

## 1. Generate a Personal Access Token

1. Sign in to GitHub.
2. Click your **profile picture** (top-right).
3. Select **Settings**.
4. In the left menu, select **Developer settings**.
5. Select **Personal access tokens**.
6. Click **Tokens (classic)**.
7. Click **Generate new token (classic)**.

---

## 2. Configure the Token

Give the token a name, for example:

```
Eclipse
```

Choose an expiration date (e.g., 90 days or No Expiration).

Under **Select scopes**, check:

```
repo
```

This allows Eclipse to push and pull repositories.

Click **Generate token**.

> **Important:** GitHub will only display the token once. Copy it immediately and store it somewhere safe.

---

## 3. Push from Eclipse

When Eclipse prompts for your GitHub credentials:

**Username**

```
your-github-username
```

Example:

```
JessySeo9955
```

**Password**

Paste the **Personal Access Token (PAT)** you generated.

> **Do not use your GitHub account password.**

Optionally, check:

```
Store in Secure Store
```

so Eclipse remembers your credentials.

Click **Log in**.

---

## 4. Verify

After logging in successfully, Eclipse should be able to:

- Commit
- Push
- Pull
- Fetch

without asking for your credentials again.

---

## Troubleshooting

### Error: `not authorized`

This usually means one of the following:

- You entered your GitHub password instead of your Personal Access Token.
- The token has expired.
- The token does not have the `repo` permission.

Generate a new Personal Access Token and try again.

---

### Eclipse keeps asking for credentials

If you entered incorrect credentials previously, clear the saved credentials.

1. Open:

```
Window → Preferences
```

2. Navigate to:

```
General → Security → Secure Storage
```

3. Remove the saved GitHub credentials.

The next time you push, Eclipse will prompt you to log in again.

---

## Reference

GitHub Documentation:

https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens
