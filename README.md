# Flow

#### A repository for messing around with Shadow CLJS and Terraform.

## Project tracking
- Create an issue and tie it to a project.
- Create a backing branch.
- Raise a draft PR, and tie it to the issue.
- The project will now be tracking the progress.

## Local development setup
- Start the Clojurescript repl in Emacs Cider with `cider-jack-in-cljs`.

## Deployment
- In `app/`, build an optimised index.js file with `shadow-cljs release :app`.
- In `infrastructure/`, create/update the infrastructure with `terraform apply`.

## Tear down
- In `infrastructure/`, tear down the infrastructure with `terraform destroy`.
