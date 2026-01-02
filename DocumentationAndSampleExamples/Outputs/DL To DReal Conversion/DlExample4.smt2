(set-logic QF_NRA)

(declare-fun y1 () Real)
(declare-fun x0 () Real)
(declare-fun b0 () Real)
(declare-fun a0 () Real)

(assert
	(not
		(and 
			(= y1 
				(+ 
					(+ x0 a0) b0
				)
			) 
			(> y1 5.0)
		)
	)
)
(check-sat)
(exit)
