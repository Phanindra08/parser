(set-logic QF_NRA_ODE)
(declare-fun Temparature_0 () Real)
(declare-fun Temparature_t () Real)
(declare-fun Temparature () Real)
(declare-fun time () Real)

(define-ode flow_1 (
    (= d/dt[Temparature] 1.0)
))

(assert (= Temparature_0 100.0))
(assert (> time 0.0))
(assert (<= time 5.0))
(assert (= Temparature_t 105.0))

(assert (and
                (= Temparature_0 100.0)
                (= Temparature_t 105.0)
                (= [Temparature_t] (integral 0. time [Temparature_0] flow_1))
))

(check-sat)
(exit)