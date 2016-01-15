(ns tst.pig-squeal.core
  (:use pig-squeal.core
        tupelo.core)
  (:require [clojure.test :refer :all]
            [clojure.string         :as str]
            [clojure.java.jdbc      :as jdbc]
            [java-jdbc.ddl          :as ddl]
            [java-jdbc.sql          :as sql]
            [schema.core            :as s]
            [tupelo.misc            :as tm]
            )
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
  (let [cmd   (create-table :tmp {:id :serial  :aa (not-null :int)  :bb :text} ) ]
    (is (= cmd "create table tmp (\n  id serial,\n  aa int not null,\n  bb text) ;"))
    (try
      (jdbc/db-do-commands db-spec cmd )
      (jdbc/db-do-commands db-spec "insert into tmp (aa,bb) values (1,'one'); ")
      (jdbc/db-do-commands db-spec "insert into tmp (aa,bb) values (2,'two'); ")
      (jdbc/query db-spec (sql/select "*" :tmp))
      (jdbc/db-do-commands db-spec (drop-table :tmp))
    (catch Exception ex
      (do (spyx ex)
          (spyx (.getNextException ex))
          (System/exit 1))))))

(deftest t-out
  (is (= "user_name, phone, id"     (tm/collapse-whitespace (out :user-name :phone :id))))
  (is (= "*"                        (tm/collapse-whitespace (out :*))))
  (is (= "count(*)"                 (tm/collapse-whitespace (out "count(*)")))))

(deftest t-select
  (is (= "select user_name, phone, id from user_info"  
         (tm/collapse-whitespace (select :user-name :phone :id :from :user-info))))
  (is (= "select * from log_data"   
         (tm/collapse-whitespace (select :* :from :log-data))))
  (is (= "select count(*) from big_table"   
         (tm/collapse-whitespace (select "count(*)" :from :big-table)))))

(deftest t-natural-join
  (try
    (jdbc/db-do-commands db-spec (drop-table-if-exists :tmp1))
    (jdbc/db-do-commands db-spec (drop-table-if-exists :tmp2))

    (jdbc/db-do-commands db-spec 
      (create-table :tmp1 {:id :serial  :aa (not-null :int)  :bb :text} ))
    (jdbc/db-do-commands db-spec "insert into tmp1 (aa,bb) values (1,'one'); ")
    (jdbc/db-do-commands db-spec "insert into tmp1 (aa,bb) values (2,'two'); ")
  ; (spyx (jdbc/query db-spec (sql/select "*" :tmp1)))

    (jdbc/db-do-commands db-spec 
      (create-table :tmp2 {:id :serial  :aa (not-null :int)  :cc :text} ))
    (jdbc/db-do-commands db-spec "insert into tmp2 (aa,cc) values (1,'cc-one'); ")
    (jdbc/db-do-commands db-spec "insert into tmp2 (aa,cc) values (2,'cc-two'); ")
  ; (spyx (jdbc/query db-spec (sql/select "*" :tmp2)))

    (newline)
    (println "live query results:")
    (spyx (jdbc/query db-spec 
            (natural-join { :ll (select :* :from :tmp1)
                            :rr (select :* :from :tmp2) 
                            :out [:*]
                          } )))

  (catch Exception ex
    (do (spyx ex)
        (spyx (.getNextException ex))
        (System/exit 1)))))

(deftest t-drop-table-if-exists
  (let [cmd   (drop-table-if-exists :tmp) ]
    (is (= "drop table if exists tmp ;\n" cmd))
    (jdbc/db-do-commands db-spec cmd ))
)
