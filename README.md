# Project Setup Guide

This repository contains two independent Maven projects:

- **JSF-ACMECollege-Skeleton** (Frontend)
- **REST-ACMECollege-Skeleton** (Backend)

Both projects share the same Git repository but are imported into Eclipse separately.

---

## 1. Clone the Repository

Clone the repository to your local machine.

```bash
git clone https://github.com/<organization-or-username>/Group-9-REST-ACMECollege.git
```

Your folder structure should look like this:

```text
Group-9-REST-ACMECollege/
├── .git
├── JSF-ACMECollege-Skeleton/
└── REST-ACMECollege-Skeleton/
```

> **Do not move or delete the `.git` folder.** It is shared by both projects.

---

## 2. Import the Backend Project

1. Open **Eclipse**.
2. Select **File → Import...**
3. Choose **Maven → Existing Maven Projects**.
4. Click **Next**.
5. Browse to:

```
Group-9-REST-ACMECollege/REST-ACMECollege-Skeleton
```

6. Eclipse will detect the `pom.xml`.
7. Click **Finish**.

---

## 3. Import the Frontend Project

Repeat the same process.

1. **File → Import...**
2. **Maven → Existing Maven Projects**
3. Browse to:

```
Group-9-REST-ACMECollege/JSF-ACMECollege-Skeleton
```

4. Click **Finish**.

Both projects should now appear in the **Package Explorer**.

---

## 4. Git Detection

Since both projects are located inside the same Git repository, **Eclipse should automatically detect the existing Git repository**.

To verify:

1. Right-click either project.
2. Select **Team**.

If you see options such as:

- Commit
- Pull
- Push
- Show in History

then Git has been detected successfully.

No additional Git configuration is required.

---

## 5. If Git Is Not Detected

If the **Team** menu displays **Share Project...** instead of Git commands:

1. Right-click the project.
2. Select **Team → Share Project...**
3. Choose **Git**.
4. Eclipse should automatically detect the existing repository located in:

```
Group-9-REST-ACMECollege/.git
```

5. Click **Finish**.

Usually, this only needs to be done once. Eclipse will often recognize the second project automatically because both projects belong to the same Git repository.

---

## 6. Verify the Repository

Open:

```
Window → Show View → Git Repositories
```

You should see a single Git repository containing both projects.

```
Group-9-REST-ACMECollege
├── JSF-ACMECollege-Skeleton
└── REST-ACMECollege-Skeleton
```

---

## 7. Update Maven Dependencies

For each project:

1. Right-click the project.
2. Select **Maven → Update Project...**
3. (Optional) Check **Force Update of Snapshots/Releases**.
4. Click **OK**.

---

## 8. You're Ready

You now have:

- Two independent Eclipse Maven projects
- One shared Git repository

You can develop both projects independently while using Eclipse's Git tools (**Commit**, **Pull**, and **Push**) to synchronize your changes with GitHub.
