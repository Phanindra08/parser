(set-logic QF_NRA)

(declare-fun y1 () Real)
(declare-fun x1 () Real)

(assert
	(and 
		(= y1 2.0) 
		(and 
			(= x1 
				(+ y1 3.0)
			) 
			(not
				(> x1 5.0)
			)
		)
	)
)
(check-sat)
(exit)
