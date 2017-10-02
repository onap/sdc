Delivery
--------
Describe how functions are packaged into run-time components.
For some components a block diagram may be useful.

.. blockdiag::
   

   blockdiag layers {
   orientation = portrait
   a -> m;
   b -> n;
   c -> x;
   m -> y;
   m -> z;
   group l1 {
	color = blue;
	x; y; z;
	}
   group l2 {
	color = yellow;
	m; n; 
	}
   group l3 {
	color = orange;
	a; b; c;
	}

   }


