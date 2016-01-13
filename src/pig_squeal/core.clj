(ns pig-squeal.core
  (:use tupelo.core)
  (:require [clojure.string :as str]
            [schema.core    :as s]
            [tupelo.misc    :as tm] )
  (:gen-class))

(s/defn drop-table :- s/Str
  "Drop (delete) a table from the database. It is an error if the table does not exist."
  [name :- s/Keyword]
  (format "drop table %s ;\n" (tm/kw->dbstr name)))

(s/defn drop-table-if-exists :- s/Str
  "Drop (delete) a table from the database, w/o error if no table exists."
  [name :- s/Keyword]
  (format "drop table if exists %s ;\n" (tm/kw->dbstr name)))

(s/defn not-null :- s/Str
  "Adds the suffix 'not null' to argument."
  [arg]
  (format "%s not null" (tm/kw->dbstr arg)))

(s/def ColSpec {s/Keyword s/Any} )    ; #todo -> { :kw  [:kw | colType] } ; #todo -> pig-squeal.types
(s/defn create-table  :- s/Str
  [name       :- s/Keyword
   colspecs   :- ColSpec]
  (let [cols-str  (str/join ","
                    (for [[col-name col-type] colspecs]
                      (format "\n  %s %s"   (tm/kw->dbstr col-name) 
                                            (tm/kw->dbstr col-type))))
        result    (format "create table %s (%s) ;" (tm/kw->dbstr name) cols-str) ]
    (println result)
    result ))
  

(defn -main []
  (println "main - enter")
)
