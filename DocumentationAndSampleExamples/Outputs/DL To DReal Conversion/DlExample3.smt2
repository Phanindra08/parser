(set-logic QF_NRA)

(declare-fun testing1Variable () Real)

(assert
	(= 
		(<= testing1Variable 7.5) 
		(not
			(= 0.567 7.0894)
		)
	)
)
(check-sat)
(exit)
