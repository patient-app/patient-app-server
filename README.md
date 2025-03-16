# Patient App Server

## How to run

1. Install openjdk-21, when typing `java --version` in the terminal the output should be `openjdk 21 2023-09-19`
2. Install docker and docker compose
3. Copy the `src/main/resources/application-dev.properties.example` file to `src/main/resources/application-dev.properties` and adjust the values if needed
4. `docker compose up -d` or (`docker-compose up -d`)
5. Visit <http://localhost:5050> in your browser to check if the database is running (email: <admin@admin.com>, password: admin)
6. On <http://localhost:5050> connect to DB -> right click "Servers" -> "Register Server" -> "name" = `patient-app-postgres`, "Host name/address" = `patient-app-postgres`, "Port" = `5432`, "Username" = `patient-app-user`, "Password" = `patient-app-password`, and "Save Password?" = `Yes`
7. `./gradlew bootRun`
8. Visit <http://localhost:8080> -> it should say: "The application is running."

## Environment Variables

###  Non-Secret Environment Variables

1. To add a new non-secret environment variable, add it to the `application-dev.properties.example`, `application-dev.properties` and `application-prod.properties` files in the `src/main/resources` folder.
2. Additionally add the .env variables to the `.kubernetes/overlays/main/kustomization.yaml` and `.kubernetes/overlays/production/kustomization.yaml` files under the configMapGenerator section (for the main and production environments)
3. Add the new environment variable to the `src/main/java/ch/uzh/ifi/imrg/patientapp/utils/EnvironmentVariables.java` file so it can be easily accessed in the code

###  Secret Environment Variables

1. To add a new non-secret environment variable, add it to the `application-dev.properties.example`, `application-dev.properties` and `application-prod.properties` files in the `src/main/resources` folder.
2. Additionally add the .env variable as a repository secret in Github under `Settings` -> `Secrets and variables` -> `New repository secret`. Add it for both main and production environments. E.g. `DB_PASSWORD_MAIN` and `DB_PASSWORD_PRODUCTION`
3. In the `.github/workflows/deploy.yml` file under the `Restart Kubernetes Deployments` section add the new secret to env section and echo it to the kubernetes overlays file in the main and production environments (see current implementation for reference)
4. Add the new environment variable to the `src/main/java/ch/uzh/ifi/imrg/patientapp/utils/EnvironmentVariables.java` file so it can be easily accessed in the code

## Main and Production Environments

###  Main Environment

- The "main" environment shows the latest changes on the main branch
- <https://backend-patient-app-main.jonas-blum.ch/>

### Production Environment

- The "production" environment shows the latest changes on the production branch
- <https://backend-patient-app-production.jonas-blum.ch/>

## Workflow: How to implement an issue

1. Look at the issue number and create a new branch (from main) with the name `issueNumber-issue-title` (e.g. `5-create-login-register-endpoint`)
2. Do some changes and add your first commit
3. As soon as you added the first commit, push the branch (so other team member are aware of your work and can already see if any problems might arise -> if everyone just codes by themselves the collaboration is usually a lot worse)
4. Create a pull request from your branch to main
5. Add the issue number to the pull request title (e.g. `5: Create login/register endpoint`)
6. In the description of the pull request, add `-closes #5` to automatically close the issue when the pull request is merged
7. Assign the pull request to yourself

8. After applying the file formatting take a look at the changes of the pull request in Github under "Files changed" to see that everything is correct
9. If everything is correct, merge the pull request with the option "Squash and merge" (so we have a nice history with one commit per issue -> otherwise the commit history is bloated with commits)
10. (Optional) If you cannot merge your branch into main due to a conflict do the following steps:

- `git checkout main`
- `git pull` (or `git reset --hard origin/main` if you have some local changes)
- `git checkout YOUR-BRANCH` (e.g. `5-create-login-register-endpoint`)
- `git rebase -i main`
- Solve the conflicts with the help of your IDE
- Do a force push of your branch `git push -f`
- Now the conflicts should be solved and you can merge your branch into main through Github (with the option "Squash and merge")

## How to update the production branch

`git push --force origin main:production`
