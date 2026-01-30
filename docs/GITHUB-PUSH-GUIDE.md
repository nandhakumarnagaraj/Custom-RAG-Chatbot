# Pushing to GitHub - Step by Step Guide

## 🚨 Current Situation

GitHub blocked your push because it detected an **OpenAI API key** in your git history (from an old commit).

---

## ✅ Solution: Clean Git History

We need to remove the sensitive data from git history and push a clean version.

### Option 1: Automated Script (Recommended)

Run the provided script:

```powershell
cd "C:\Users\nandh\Desktop\RAG ChatBot\Backend\rag"
.\scripts\clean-git-history.ps1
```

This will:
1. Create a backup of your code
2. Remove the old git history
3. Create a fresh repository
4. Prepare for clean push

Then push:
```bash
git push -f origin main
```

---

### Option 2: Manual Steps

If you prefer to do it manually:

**Step 1: Create Backup**
```powershell
cd "C:\Users\nandh\Desktop\RAG ChatBot\Backend"
Copy-Item -Path "rag" -Destination "rag-backup" -Recurse
```

**Step 2: Remove Git History**
```powershell
cd rag
Remove-Item -Path ".git" -Recurse -Force
```

**Step 3: Initialize Fresh Repository**
```bash
git init
git add .
git commit -m "Initial commit - Clean history without secrets"
```

**Step 4: Add Remote and Push**
```bash
git remote add origin https://github.com/nandhakumarnagaraj/Custom-RAG-Chatbot.git
git branch -M main
git push -f origin main
```

---

## 🔒 What Was Fixed

1. ✅ Removed `spring-ai-overview.txt` (contained example API key)
2. ✅ Updated `.gitignore` with security patterns
3. ✅ Created security documentation
4. ✅ Added git history cleaning script

---

## 📋 Files Changed

### Removed
- `src/test/resources/test-documents/spring-ai-overview.txt`

### Updated
- `.gitignore` - Added security patterns

### Added
- `docs/SECURITY.md` - Security best practices
- `scripts/clean-git-history.ps1` - History cleaning script
- `docs/GITHUB-PUSH-GUIDE.md` - This file

---

## 🎯 Quick Start (Do This Now)

```powershell
# 1. Navigate to project
cd "C:\Users\nandh\Desktop\RAG ChatBot\Backend\rag"

# 2. Run the clean history script
.\scripts\clean-git-history.ps1

# 3. After script completes, force push
git push -f origin main
```

---

## ⚠️ Important Notes

### About Force Push

`git push -f` (force push) will **overwrite** the remote repository with your local version.

**This is safe in your case because:**
- You're the only developer
- The old history contains secrets
- We're replacing it with a clean version

**Never force push if:**
- Others are working on the same repository
- You're not sure what you're doing
- You haven't created a backup

### After Successful Push

1. ✅ Verify on GitHub that the code is there
2. ✅ Check that no secrets are visible
3. ✅ Delete the backup if everything looks good
4. ✅ Continue development normally

---

## 🔍 Verifying the Push

After pushing, check:

1. **Go to GitHub:**
   ```
   https://github.com/nandhakumarnagaraj/Custom-RAG-Chatbot
   ```

2. **Verify files are there:**
   - README.md
   - src/
   - docs/
   - scripts/
   - sample-data/

3. **Check commit history:**
   - Should only show "Initial commit - Clean history without secrets"
   - No old commits with API keys

4. **Search for secrets:**
   - Use GitHub's search: `sk-` or `api-key`
   - Should find nothing sensitive

---

## 🛡️ Preventing Future Issues

### Before Every Commit

```bash
# Check what you're committing
git diff

# Search for potential secrets
git grep -i "api.key"
git grep -i "password"
git grep -i "secret"
```

### Use Environment Variables

Instead of hardcoding secrets:

```properties
# ❌ Don't do this
spring.ai.openai.api-key=sk-1234567890

# ✅ Do this
spring.ai.openai.api-key=${OPENAI_API_KEY:}
```

### Keep .gitignore Updated

Already added to `.gitignore`:
```gitignore
### Security - Never commit these! ###
**/application-local.properties
**/*-secret.properties
**/*.key
.env
```

---

## 🎉 Success Checklist

After following this guide:

- [ ] Backup created
- [ ] Git history cleaned
- [ ] Fresh repository initialized
- [ ] Force pushed to GitHub
- [ ] Verified on GitHub website
- [ ] No secrets visible
- [ ] README.md displays correctly
- [ ] All files present

---

## 🆘 If Push Still Fails

### Error: "push declined due to repository rule violations"

**Cause:** GitHub still detects secrets

**Solution:**
1. Check what GitHub flagged
2. Remove that file/content
3. Commit the change
4. Try pushing again

### Error: "failed to push some refs"

**Cause:** Remote has changes you don't have

**Solution:**
```bash
git pull origin main --rebase
git push origin main
```

Or force push (if you're sure):
```bash
git push -f origin main
```

### Error: "remote rejected"

**Cause:** Repository protection rules

**Solution:**
1. Go to GitHub repository settings
2. Branches → Branch protection rules
3. Temporarily disable protection
4. Push
5. Re-enable protection

---

## 📞 Need Help?

If you encounter issues:

1. **Check the error message** - It usually tells you what's wrong
2. **Read `docs/SECURITY.md`** - Detailed security guide
3. **Check `docs/TROUBLESHOOTING.md`** - Common issues
4. **Create a GitHub issue** - If problem persists

---

## 🎓 Learning Resources

- [GitHub Secret Scanning](https://docs.github.com/en/code-security/secret-scanning)
- [Git Force Push](https://git-scm.com/docs/git-push#Documentation/git-push.txt--f)
- [Removing Sensitive Data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)

---

**Last Updated:** 2026-01-30

**Status:** Ready to push! Run the script and force push.
