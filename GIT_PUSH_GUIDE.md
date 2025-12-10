# Git Push Guide - Required Files Only

## 📦 Files to Push to Git

### ✅ Required Files (Push These)

#### Root Directory
```
Revcart_Microservices/
├── .gitignore                    # Git ignore rules
├── docker-compose.yml            # Docker orchestration
├── .env.example                  # Environment template
├── Jenkinsfile                   # CI/CD pipeline
├── README.md                     # Project documentation
├── DOCKER_GUIDE.md              # Docker guide
├── AWS_DEPLOYMENT_GUIDE.md      # AWS deployment guide
├── SIMPLE_DEPLOYMENT.md         # Simple deployment guide
├── ec2-user-data.sh             # EC2 setup script
└── init-mysql.sql               # MySQL init script
```

#### Each Microservice Directory
```
user-service/
├── src/                         # Source code
├── pom.xml                      # Maven config
├── Dockerfile                   # Docker build
└── README.md                    # Service docs

(Same for all 8 services + gateway)
```

#### Frontend
```
Frontend/
├── src/                         # Angular source
├── public/                      # Static assets
├── package.json                 # NPM dependencies
├── package-lock.json            # NPM lock file
├── angular.json                 # Angular config
├── tsconfig.json                # TypeScript config
├── tailwind.config.js           # Tailwind config
├── Dockerfile                   # Docker build
├── nginx.conf                   # Nginx config
└── README.md                    # Frontend docs
```

### ❌ Files NOT to Push (Excluded by .gitignore)

```
# Build artifacts
target/                          # Maven builds
node_modules/                    # NPM packages
dist/                           # Angular builds
.angular/                       # Angular cache

# Environment & Secrets
.env                            # Environment variables
*.pem                           # SSH keys
*.key                           # Private keys

# IDE files
.idea/                          # IntelliJ
.vscode/                        # VS Code
*.iml                           # IntelliJ modules

# Logs & Temp
*.log                           # Log files
*.tmp                           # Temp files
*.sql                           # Database dumps

# OS files
.DS_Store                       # macOS
Thumbs.db                       # Windows
```

## 🚀 How to Push to Git

### First Time Setup

```bash
# Navigate to project root
cd C:\Users\DINESH\Desktop\RevCartcopy\Revcart_Microservices

# Initialize Git (if not already)
git init

# Add remote repository
git remote add origin <YOUR_GITHUB_REPO_URL>

# Check what will be committed
git status

# Add all files (respects .gitignore)
git add .

# Commit
git commit -m "Initial commit: RevCart microservices with CI/CD"

# Push to main branch
git push -u origin main
```

### Regular Updates

```bash
# Check changes
git status

# Add changes
git add .

# Commit with message
git commit -m "Your commit message"

# Push
git push origin main
```

## 📋 Pre-Push Checklist

Before pushing, verify:

1. ✅ `.gitignore` is in place
2. ✅ No `.env` file (only `.env.example`)
3. ✅ No `target/` directories
4. ✅ No `node_modules/`
5. ✅ No `.pem` or `.key` files
6. ✅ No database dumps (`.sql` files)
7. ✅ No IDE-specific files

### Check What Will Be Pushed

```bash
# See what files are tracked
git ls-files

# See what will be committed
git status

# See ignored files
git status --ignored
```

## 🔒 Security Check

### Never Push These:

```bash
# Check for sensitive data
git grep -i "password"
git grep -i "secret"
git grep -i "api_key"

# If found, remove from history
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch <FILE_PATH>" \
  --prune-empty --tag-name-filter cat -- --all
```

### Update Passwords in Code

Before pushing, replace hardcoded passwords with environment variables:

```yaml
# ❌ Bad (in application.yml)
password: Mahidinesh@07

# ✅ Good
password: ${DB_PASSWORD:defaultpass}
```

Then set in `.env` (not pushed to Git):
```bash
DB_PASSWORD=Mahidinesh@07
```

## 📊 Verify Repository Size

```bash
# Check repository size
git count-objects -vH

# If too large, clean up
git gc --aggressive --prune=now
```

## 🎯 Essential Files Summary

### Must Push (17 files + source code):
1. `.gitignore`
2. `docker-compose.yml`
3. `.env.example`
4. `Jenkinsfile`
5. `README.md`
6. `DOCKER_GUIDE.md`
7. `AWS_DEPLOYMENT_GUIDE.md`
8. `SIMPLE_DEPLOYMENT.md`
9. `ec2-user-data.sh`
10. `init-mysql.sql`
11. All `Dockerfile`s (10 files)
12. All `pom.xml` files (9 files)
13. All `src/` directories
14. Frontend `package.json`
15. Frontend `src/`
16. Frontend configs

### Total Size: ~50-100 MB (without build artifacts)

## 🔄 Quick Push Commands

```bash
# Quick commit and push
git add .
git commit -m "Update: <description>"
git push

# Push specific files only
git add Jenkinsfile docker-compose.yml
git commit -m "Update CI/CD configuration"
git push

# Undo last commit (before push)
git reset --soft HEAD~1

# Force push (careful!)
git push --force origin main
```

## ✅ Final Verification

After pushing, verify on GitHub/GitLab:

1. Check repository size (should be < 100 MB)
2. Verify no `.env` file
3. Verify no `target/` or `node_modules/`
4. Check Jenkinsfile is present
5. Check all Dockerfiles are present
6. Verify README.md displays correctly

## 🎉 Ready to Push!

Your repository should contain:
- ✅ Source code
- ✅ Configuration files
- ✅ Docker files
- ✅ CI/CD pipeline
- ✅ Documentation

And exclude:
- ❌ Build artifacts
- ❌ Dependencies
- ❌ Secrets
- ❌ IDE files
- ❌ Logs

Now Jenkins can pull and build everything! 🚀
