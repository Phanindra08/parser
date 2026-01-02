(set-logic QF_NRA)

(declare-fun z1 () Real)
(declare-fun y1 () Real)
(declare-fun x0 () Real)
(declare-fun b0 () Real)
(declare-fun a0 () Real)

(assert
	(and 
		(= y1 
			(+ 
				(+ x0 a0) b0
			)
		) 
		(and 
			(= z1 
				(+ y1 3.0)
			) 
			(> z1 5.0)
		)
	)
)
(check-sat)
(exit)
