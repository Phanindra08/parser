(set-logic QF_NRA)

(declare-fun x0 () Real)
(declare-fun x () Real)
(declare-fun x1 () Real)
(declare-fun time () Real)

(define-ode flow_1 (
	(= d/dt[x] 1.0)
))

(assert
	(and 
		(>= x0 0.0) 
		(not
			(and 
				(> time 0.0) 
				(and 
					(<= time 5.0) 
					(and 
						(= [x1] (integral 0. time [x0] flow_1)) 
						(<= x1 10.0)
					)
				)
			)
		)
	)
)
(check-sat)
(exit)
