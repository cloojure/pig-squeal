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
    (jdbc/db-do-commands db-spec "insert into tmp (aa,bb) values (2,'two'); ")
    (jdbc/query db-spec (sql/select "*" :tmp))
    (jdbc/db-do-commands db-spec (drop-table :tmp))
  (catch Exception ex
    (do (spyx ex)
        (spyx (.getNextException ex))
        (System/exit 1)))))

(deftest t-natural-join
  (try
    (jdbc/db-do-commands db-spec (drop-table-if-exists :tmp1))
    (jdbc/db-do-commands db-spec (drop-table-if-exists :tmp2))

    (jdbc/db-do-commands db-spec 
      (create-table :tmp1 {:id :serial  :aa (not-null :int)  :bb :text} ))
    (jdbc/db-do-commands db-spec "insert into tmp1 (aa,bb) values (1,'one'); ")
    (jdbc/db-do-commands db-spec "insert into tmp1 (aa,bb) values (2,'two'); ")
    (spyx (jdbc/query db-spec (sql/select "*" :tmp1)))

    (jdbc/db-do-commands db-spec 
      (create-table :tmp2 {:id :serial  :aa (not-null :int)  :cc :text} ))
    (jdbc/db-do-commands db-spec "insert into tmp2 (aa,cc) values (1,'cc-one'); ")
    (jdbc/db-do-commands db-spec "insert into tmp2 (aa,cc) values (2,'cc-two'); ")
    (spyx (jdbc/query db-spec (sql/select "*" :tmp2)))

    (spyx (jdbc/query db-spec 
            (natural-join { :q1 "select * from tmp1" 
                    :q2 "select * from tmp2" } )))

  (catch Exception ex
    (do (spyx ex)
        (spyx (.getNextException ex))
        (System/exit 1)))))

(deftest t-drop-table-if-exists
  (jdbc/db-do-commands db-spec (spyx (drop-table-if-exists :tmp) ))
)
