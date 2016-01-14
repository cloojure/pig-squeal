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

; (def KeywordStrList  [ (s/maybe s/Keyword)  ; #todo make this work
;                        (s/maybe s/Str) ] )

; #todo maybe do:  (select :user-info [:user-name :phone :id] )
;                            ^table       ^col       ^col  ^col
; #todo maybe do:  (select :user-info [:user-name :phone :id] 
;                          :where    ... 
;                          :group-by ... 
;                          :order-by ...  } )
;       e.g.       (select [tbl-name col-lst & options-map] )
(s/defn select :- s/Str
  "Format SQL select statement; eg: 
     (select :user-name :phone :id :from :user-info) 
     (select :*                    :from :log-data) 
     (select \"count(*)\"          :from :big-table)
   "  ; #todo add examples to all docstrings
  [& args :- [s/Any]]
  (let [num-cols    (- (count args) 2)
        cols        (take num-cols args)
        tails       (drop num-cols args)
        from-kw     (first tails)
        _ (assert (= :from from-kw))    ; #todo test this
        table       (last tails) 
        cols-str    (str/join ", "
                      (mapv tm/kw->dbstr cols))
        table-str   (tm/kw->dbstr table) 
        result      (format "select %s from %s" cols-str table-str)
  ]
    result))

(s/defn natural-join :- s/Str
  "Performs a join between two sub-expressions."
  [exp-map :- ts/KeyMap] ; #todo only tested for 2-way join for now
  (assert (= 2 (count exp-map)))
  (let [[p1-alias p1-exp]   (first  exp-map)  ; #todo verify "select .*"
        [p2-alias p2-exp]   (second exp-map)  ; #todo verify "select .*"
        result  (tm/collapse-whitespace 
                  ; #todo need utils for shifting lines (right)
                  (let [p1-alias-str  (tm/kw->dbstr p1-alias) 
                        p2-alias-str  (tm/kw->dbstr p2-alias) ]
                    (format "with
                               %s as (%s),
                               %s as (%s)
                             select * from 
                               %s natural join %s ;" 
                        p1-alias-str p1-exp 
                        p2-alias-str p2-exp 
                        p1-alias-str p2-alias-str
                      )))
  ]
    (println result)
    result
  ))  
        ; select
        ;   dashboards.name,
        ;   log_counts.ct
        ; from dashboards 
        ; join (
        ;   select 
        ;     distinct_logs.dashboard_id, 
        ;     count(1) as ct
        ;   from (
        ;     select distinct 
        ;       dashboard_id, 
        ;       user_id
        ;     from time_on_site_logs
        ;   ) as distinct_logs
        ;   group by distinct_logs.dashboard_id
        ; ) as log_counts 
        ; on log_counts.dashboard_id = dashboards.id
        ; order by log_counts.ct desc

        ; WITH 
        ;   regional_sales AS (
        ;     SELECT region, SUM(amount) AS total_sales
        ;     FROM orders
        ;     GROUP BY region
        ;   ), 
        ;   top_regions AS (
        ;     SELECT region
        ;     FROM regional_sales
        ;     WHERE total_sales > (SELECT SUM(total_sales)/10 FROM regional_sales)
        ;   )
        ; SELECT 
        ;   region,
        ;   product,
        ;   SUM(quantity) AS product_units,
        ;   SUM(amount) AS product_sales
        ; FROM orders
        ; WHERE region IN (SELECT region FROM top_regions)
        ; GROUP BY region, product;

(defn -main []
  (println "main - enter")
)
