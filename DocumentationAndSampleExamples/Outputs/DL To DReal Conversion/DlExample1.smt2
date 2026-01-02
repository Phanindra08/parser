(set-logic QF_NRA)

(declare-fun testing3Variable () Real)
(declare-fun testing () Real)
(declare-fun testing2Variable () Real)
(declare-fun testing1Variable () Real)

(assert
	(and 
		(and 
			(>= testing1Variable 7.5) 
			(> testing2Variable 7.0894)
		) 
		(not
			(< testing testing3Variable)
		)
	)
)
(check-sat)
(exit)
