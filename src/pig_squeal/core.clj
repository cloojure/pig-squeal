(ns pig-squeal.core
  (:use tupelo.core)
  (:require [clojure.string :as str]
            [schema.core    :as s]
            [tupelo.schema  :as ts]
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

(s/def ColSpec ts/KeyMap )    ; #todo -> { :kw  [:kw | colType] } ; #todo -> pig-squeal.types
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

; (s/defn select ...)
;   alan=> select * from tmp1; select * from tmp2;
;    id | aa | bb  
;   ----+----+-----
;     1 |  1 | one
;     2 |  2 | two
;   (2 rows)
;
;    id | aa |   cc   
;   ----+----+--------
;     1 |  1 | cc-one
;     2 |  2 | cc-two
;   (2 rows)
;
;   alan=> select xx.aa a,xx.bb b,yy.cc  from tmp1 xx natural join (select * from tmp2) yy;
;    a |  b  |   cc   
;   ---+-----+--------
;    1 | one | cc-one
;    2 | two | cc-two
;   (2 rows)
;
(s/defn natural-join :- s/Str
  "Performs a join between two sub-expressions."
  [exp-map :- ts/KeyMap] ; #todo only tested for 2-way join for now
  (assert (= 2 (count exp-map)))
  (let [[p1-alias p1-exp]       (first  exp-map)
        [p2-alias p2-exp]   (second exp-map) 
        result  (tm/collapse-whitespace 
                  (format " %s as %s
                         natural join (
                          %s ) as %s" 
                    p1-exp (tm/kw->dbstr p1-alias)
                    p2-exp (tm/kw->dbstr p2-alias)))
  ]
    (println result)
    result
  ))
  

(defn -main []
  (println "main - enter")
)
