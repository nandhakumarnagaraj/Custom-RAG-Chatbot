# Security Best Practices

## 🔒 Handling Secrets and API Keys

This guide explains how to properly handle sensitive information in your RAG ChatBot project.

---

## ⚠️ What NOT to Commit

**NEVER commit these to Git:**

- ❌ API keys (OpenAI, Azure, etc.)
- ❌ Database passwords
- ❌ Private keys (.key, .pem files)
- ❌ Certificates (.p12, .jks files)
- ❌ Environment-specific configurations
- ❌ Personal access tokens
- ❌ OAuth secrets

---

## ✅ How to Handle Secrets Properly

### Method 1: Environment Variables (Recommended)

**Step 1: Remove secrets from application.properties**

Instead of:
```properties
# ❌ DON'T DO THIS
spring.ai.openai.api-key=sk-1234567890abcdef
```

Use placeholders:
```properties
# ✅ DO THIS
spring.ai.openai.api-key=${OPENAI_API_KEY:}
```

**Step 2: Set environment variables**

Windows PowerShell:
```powershell
$env:OPENAI_API_KEY="your-actual-key-here"
```

Linux/Mac:
```bash
export OPENAI_API_KEY="your-actual-key-here"
```

**Step 3: Run your application**

The application will read the key from the environment variable.

---

### Method 2: Local Properties File (Not Committed)

**Step 1: Create application-local.properties**

Create `src/main/resources/application-local.properties`:
```properties
# This file is in .gitignore and won't be committed
spring.ai.openai.api-key=your-actual-key-here
```

**Step 2: Activate the profile**

Run with:
```bash
java -jar app.jar --spring.profiles.active=local
```

Or in application.properties:
```properties
spring.profiles.active=local
```

**Step 3: Ensure it's in .gitignore**

Already added:
```gitignore
**/application-local.properties
**/application-dev.properties
```

---

### Method 3: .env File (For Development)

**Step 1: Create .env file**

Create `.env` in project root:
```bash
OPENAI_API_KEY=your-actual-key-here
CHROMA_PORT=8000
```

**Step 2: Load in your application**

Add dependency to `pom.xml`:
```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

**Step 3: Ensure .env is in .gitignore**

Already added:
```gitignore
.env
.env.local
```

---

## 🧹 If You Accidentally Committed Secrets

### Option 1: Remove from Latest Commit (If Not Pushed)

```bash
# Remove the file
git rm --cached src/main/resources/application.properties

# Add it to .gitignore
echo "src/main/resources/application.properties" >> .gitignore

# Amend the commit
git commit --amend -m "Remove secrets"
```

### Option 2: Clean Git History (If Already Pushed)

**⚠️ WARNING: This rewrites history!**

Use the provided script:
```powershell
.\scripts\clean-git-history.ps1
```

Or manually:
```bash
# 1. Backup your code
cp -r . ../rag-backup

# 2. Remove .git directory
rm -rf .git

# 3. Initialize fresh repository
git init
git add .
git commit -m "Initial commit - Clean history"

# 4. Force push
git remote add origin <your-repo-url>
git push -f origin main
```

### Option 3: Use BFG Repo-Cleaner

```bash
# Install BFG
# Download from: https://rtyley.github.io/bfg-repo-cleaner/

# Remove secrets
java -jar bfg.jar --replace-text secrets.txt

# Clean up
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push
git push -f origin main
```

---

## 📋 Current Project Configuration

### What's Already Protected

The `.gitignore` file already includes:

```gitignore
### Security - Never commit these! ###
**/application-local.properties
**/application-dev.properties
**/*-secret.properties
**/*.key
**/*.pem
**/*.p12
**/*.jks
**/secrets/
.env
.env.local
```

### Current Configuration

`src/main/resources/application.properties` uses **Ollama** (local AI), so:

✅ **No API keys needed!**  
✅ **Runs completely offline**  
✅ **No external API calls**  
✅ **No secrets to manage**

---

## 🔍 Checking for Secrets

### Before Committing

```bash
# Search for potential secrets
git grep -i "api.key"
git grep -i "password"
git grep -i "secret"
git grep -i "token"

# Check what you're about to commit
git diff --cached
```

### Using Git Hooks

Create `.git/hooks/pre-commit`:

```bash
#!/bin/sh

# Check for potential secrets
if git diff --cached | grep -i "api.key\|password\|secret\|token"; then
    echo "⚠️  WARNING: Potential secret detected!"
    echo "Please review your changes before committing."
    exit 1
fi
```

Make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

---

## 🛡️ GitHub Secret Scanning

GitHub automatically scans for:
- API keys
- OAuth tokens
- Private keys
- Database credentials

If detected, your push will be **blocked** (as you experienced).

**What to do:**
1. Remove the secret from your code
2. Clean git history (if already committed)
3. **Revoke the exposed secret** (very important!)
4. Generate a new secret
5. Store it properly (environment variables)

---

## 🔐 Revoking Exposed Secrets

If you accidentally exposed an API key:

### OpenAI
1. Go to https://platform.openai.com/api-keys
2. Find the exposed key
3. Click "Revoke"
4. Generate a new key

### GitHub
1. Go to Settings → Developer settings → Personal access tokens
2. Find the token
3. Click "Delete"
4. Generate a new token

### General
- Change the password/key immediately
- Check for unauthorized usage
- Enable 2FA if available

---

## ✅ Best Practices Checklist

Before committing:

- [ ] No API keys in code
- [ ] No passwords in code
- [ ] No tokens in code
- [ ] Secrets in environment variables or local config
- [ ] Local config files in .gitignore
- [ ] Reviewed `git diff` before commit
- [ ] No sensitive test data

Before pushing:

- [ ] Reviewed all commits
- [ ] No secrets in commit history
- [ ] .gitignore is up to date
- [ ] Environment variables documented in README

---

## 📚 Additional Resources

- [GitHub Secret Scanning](https://docs.github.com/en/code-security/secret-scanning)
- [Git Secrets Tool](https://github.com/awslabs/git-secrets)
- [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/)
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

---

## 🎯 For This Project

Since you're using **Ollama** (local AI):

✅ **You don't need any API keys!**

The current configuration is already secure:
- No external API calls
- No secrets required
- Runs completely offline

Just make sure to:
1. Clean the git history (remove old OpenAI config)
2. Keep using Ollama
3. Don't commit any future secrets

---

**Last Updated:** 2026-01-30
