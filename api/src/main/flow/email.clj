(ns flow.email
  (:require [ses-mailer.core :as mailer]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]))


(defn send-email!
  "Sends and email with the body provided, and with the
   subject provided to the email address provided."
  [email-address subject {:keys [html text] :as body}]

  (when-not (s/valid? :email/email-address email-address)
    (expound/expound :email/email-address email-address)
    (throw (IllegalStateException. "the email address violate specification")))

  (when-not (s/valid? :email/subject subject)
    (expound/expound :email/subject subject)
    (throw (IllegalStateException. "the email subject violate specification")))

  ;; TODO - use separate specs for the free text vs HTML text
  (when-not (s/valid? :email/body body)
    (expound/expound :email/body body)
    (throw (IllegalStateException. "the email body violate specification")))

  (mailer/send-email
   {}
   "Flow <noreply@flow.keigo.io>"
   email-address
   subject
   {:html-body html
    :text-body text}))
