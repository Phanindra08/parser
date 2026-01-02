(set-logic QF_NRA)

(declare-fun x () Real)
(declare-fun testing2Variable () Real)
(declare-fun testing1Variable () Real)

(assert
	(and 
		(> testing1Variable 7.5) 
		(not
			(= testing2Variable x)
		)
	)
)
(check-sat)
(exit)
