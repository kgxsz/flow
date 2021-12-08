# Flow

#### A repository for messing around with Shadow CLJS and Terraform.

## Project tracking
- Create an issue and tie it to a project.
- Create a backing branch.
- Raise a draft PR, and tie it to the issue.
- The project will now be tracking the progress.


## Local development setup

### Self signed certificate setup for api
- Create and trust a self signed root SSL server certificate with:
  - Name as `api.localhost`.
  - Common name as `api.localhost`.
  - URI as `api.localhost`.
  - DNS name as `api.localhost`.
  - Empty IP Address.
- Export the certificate to `api/ssl/Certificates.p12` and choose a keystore password.
- In `api/ssl/` generate the `keystore.jks` with: 
  `keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore Certificates.p12`.
- Ensure that the destination and source keystore passwords are equal to the keystore password above.

### Self signed certificate setup for app
- Create and trust a self signed root SSL server certificate with:
  - Name as `localhost`.
  - Common name as `localhost`.
  - URI as `localhost`.
  - DNS name as `localhost`.
  - Empty IP Address.
- Export the certificate to `app/ssl/Certificates.p12` and choose a keystore password.
- In `app/ssl/` generate the `keystore.jks` with: 
  `keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore Certificates.p12`.
- Ensure that the destination and source keystore passwords are equal to the keystore password above.

### Local database setup
- Get a local DynamoDB instance running with: `docker run -p 8000:8000 amazon/dynamodb-local`.

### Local api development setup
- In `api/` setup the environment variables:
  - `KEYSTORE_PASSWORD` as determined above.
  - `CORS_ORIGIN` as `https://localhost:8080`.
  - `COOKIE_STORE_KEY` as `1234123412341234`.
  - `DB_ENDPOINT` as `http://localhost:8000`.
- In `api/` start the REPL with `clj -M:repl`.
- Connect to the api's Clojure REPL, load `flow.dev`.
- Start the local server with: `(server)`.
- Create a local DynamoDB table with: `(create-table)`.
- Seed the local DynamoDB table with: `(seed-table)`.
- The api will be running at `https://api.localhost:443`.

### Local app development setup
- In `app/` setup the environment variables:
  - `KEYSTORE_PASSWORD` as determined above.
- In `app/` run `npm install` to prepare some dependencies.
- In `app/` start the auto JS compilation with `clj -M:dev/js`.
- In `app/` start the auto CSS compilation with `clj -M:dev/css`.
- Connect to the app's Clojure REPL, and start the Clojurescript REPL with `(repl)`.
- The app will be running at `https://localhost:8080`.


## Testing

### Unit
- In `api/` setup the environment variables:
  - `CORS_ORIGIN` as `https://localhost:8080`.
- In `api/` run the unit tests with `clj -M:test/unit`.
- Alternatively, in the api's Clojure REPL, run `(kaocha/run :unit)`.

### Feature
- In `api/` setup the environment variables:
  - `CORS_ORIGIN` as `https://localhost:8080`.
- In `api/` run the feature tests with `clj -M:test/feature`.
- Alternatively, in the api's Clojure REPL, run `(kaocha/run :feature)`.


## Remote deployment

### Create the assets
- In `app/` build an optimised index.js file with `clj -M:release/js`.
- In `app/` build an optimised index.css file with `clj -M:release/css`.
- In `api/` clear out previous assets with `rm -rf classes && mkdir classes && rm -rf target && mkdir target`.
- In `api/` compile the API with `clj -M:compile`.
- In `api/` zip the API with `clj -M:zip mach.pack.alpha.aws-lambda target/flow.zip -C:compile -R:compile`

### Deploy the infrastructure and assets
- In `infrastructure/` setup the environment variables:
  - `TF_VAR_cookie_store_key` as the 16 byte secret key set in production.
- In `infrastructure/` initialise terraform if required using `terraform init`.
- In `infrastructure/` update the remote assets with `terraform apply`.

## Tear down
- Manually backup the DynamoDB table in the AWS UI as a precaution.
- In `infrastructure/`, tear down remote assets with `terraform destroy`.
