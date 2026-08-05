# CST8277 Group Project — Delegation Plan

> Rule: every entity is a **vertical slice** (JPA → NamedQuery → Resource → JUnit → JSF page) so each person touches every knowledge node for the final. Anything with **no clean cleavage is owned by ONE person**, not a group, to avoid merge conflicts.

---

## Simon — Course (+ shared foundation owner)

**Owns the shared plumbing** (single owner = no conflicts):
- [ ] Task Two — custom auth: `CustomIdentityStoreJPAHelper.findUserByName()` (use the DB)
- [ ] `SecurityUser`, `SecurityRole`, `user_has_role` JPA mapping (no Resource for these)
- [ ] Populate `security_user` / `security_role` / `user_has_role` via raw SQL
- [ ] Owns the `ACMECollegeService.java` file structure (others append into their own commented block)

**Entity vertical slice — Course:**
- [ ] JPA annotations on `Course` + NamedQueries (`LEFT JOIN FETCH` where needed)
- [ ] Jackson annotations
- [ ] `CourseResource` — full CRUD
- [ ] `@RolesAllowed` security rules on Course endpoints
- [ ] ~7 JUnit tests (CRUD + negative + role)
- [ ] JSF page: **Course Management**

---

## Jessy — Professor + StudentClub

**Entity vertical slices:**
- [ ] `Professor` — JPA + NamedQueries + Jackson
- [ ] `ProfessorResource` — full CRUD + security
- [ ] `StudentClub` — JPA + NamedQueries with `LEFT JOIN FETCH sc.studentMembers`
- [ ] `StudentClub` — `@JsonSerialize` to expose **member count only** + `@JsonIgnore` on member list
- [ ] `StudentClubResource` — full CRUD + security (note: *any* user can read clubs; only ADMIN can CRUD)
- [ ] ~7 JUnit tests across Professor + StudentClub (CRUD + negative + role)

**JSF pages:**
- [ ] Professor Management
- [ ] Student Club Management
- [ ] Club Membership Registration

---

## Hadi— CourseRegistration (owns all associations + team lead)

**Owns the relationship/join entity and every "assign" operation** (single owner = no FK conflicts):
- [ ] Task Three — `SecurityUser` ↔ `Student` 1:1 relationship
- [ ] `CourseRegistration` — JPA + NamedQueries (`LEFT JOIN FETCH` to Course/Professor/Student)
- [ ] `CourseRegistrationResource` — full CRUD + security
- [ ] Endpoint: **associate Course → registration** (ADMIN only)
- [ ] Endpoint: **assign Professor → registration** (ADMIN only)
- [ ] Endpoint: **assign Grade → registration** (ADMIN only)
- [ ] ~7 JUnit tests (CRUD + associations + negative + role)

**JSF pages:**
- [ ] Course Registration (create)
- [ ] Assign Professor
- [ ] Assign Grade

**Team-lead / cross-cutting (single owner):**
- [ ] Copy updated entities REST-ACMECollege → JSF-ACMECollege after each merge
- [ ] Run `clean install test surefire-report:report site -DgenerateReports=true` and stitch the surefire report (verify ≥ 20 tests)
- [ ] Write `ReportAndPeerReview.doc` (⚠️ missing = grade of 0)
- [ ] Export & submit `Group-XX-REST-ACMECollege.zip` + `Group-XX-JSF-ACMECollege.zip`

---

## Merge-conflict hotspots (agreed rules)

| File | Rule |
|------|------|
| `ACMECollegeService.java` | Person A owns file; B & C add methods only inside their own `// --- B ---` / `// --- C ---` block |
| JSF nav/menu template | One `<li>` per feature, added independently; no reformatting others' lines |
| `Bundle.properties` | Append your keys at the bottom under a `# <name>` comment — never edit existing keys |
| Entities (shared across both projects) | Only Person C copies REST → JSF, and only after an entity is merged & green |

## Shared conventions
- Fetch is always **LAZY**; fix `LazyInitializationException` with `LEFT JOIN FETCH` in NamedQueries.
- No Resource for `security_user`, `security_role`, `user_has_role`.
- Security users: `admin/admin` (ADMIN_ROLE), `cst8277/8277` (USER_ROLE).
- Everyone must be present at the Week 14 demo and able to answer questions on **any** part.
