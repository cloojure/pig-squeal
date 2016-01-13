(ns tst.pig-squeal.core
  (:use pig-squeal.core
        tupelo.core)
  (:require [clojure.test :refer :all]
            [pig-squeal.core :refer :all]
            [clojure.string         :as str]
            [clojure.java.jdbc      :as jdbc]
            [schema.core            :as s]
            [java-jdbc.ddl          :as ddl]
            [java-jdbc.sql          :as sql] )
  (:import  com.mchange.v2.c3p0.ComboPooledDataSource)
)

; #todo git basic pg_hba.conf into git
; #todo blog about scm of pg *.conf files
(def db-spec
  { :classname    "org.postgresql.Driver"
    :subprotocol  "postgresql"
    :subname      "//localhost:5432/alan"    ; database="alan"  #todo -> pgsql
    ; Not needed for a non-secure local database...
    ;   :user      "bilbo"
    ;   :password  "secret"
    } )

; int integer int4 int8 
; numeric
(deftest t-create-table
  (try
    (jdbc/db-do-commands db-spec 
      (create-table :tmp {:id :serial  :aa (not-null :int)  :bb :text} ))
    (jdbc/db-do-commands db-spec "insert into tmp (aa,bb) values (1,'one'); ")
    (spyx (jdbc/query db-spec (sql/select "*" :tmp)))
  (catch Exception ex
    (do (spyx ex)
        (spyx (.getNextException ex))
        (System/exit 1))))

)

(deftest t-drop-table-if-exists
  (jdbc/db-do-commands db-spec (spyx (drop-table-if-exists :tmp) ))
)
