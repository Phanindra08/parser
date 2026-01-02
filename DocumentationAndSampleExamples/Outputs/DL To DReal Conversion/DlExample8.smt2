(set-logic QF_NRA)

(declare-fun x () Real)
(declare-fun x1 () Real)
(declare-fun x2 () Real)
(declare-fun time () Real)

(define-ode flow_1 (
	(= d/dt[x] 1.0)
))

(assert
	(not
		(and 
			(= x1 1.0) 
			(and 
				(> time 0.0) 
				(and 
					(<= time 5.0) 
					(and 
						(>= x1 1.0) 
						(and 
							(= [x2] (integral 0. time [x1] flow_1)) 
							(> x2 5.0)
						)
					)
				)
			)
		)
	)
)
(check-sat)
(exit)
