(set-logic QF_NRA)

(declare-fun y1 () Real)
(declare-fun x1 () Real)

(assert
	(and 
		(and 
			(> y1 3.0) 
			(> x1 y1)
		) 
		(not
			(> x1 5.0)
		)
	)
)
(check-sat)
(exit)
