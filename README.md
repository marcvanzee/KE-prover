# KE-prover

#### An Object-Oriented Implementation of a KE Tableau Prover

Assignment for Automatisch Redeneren by Gerard Vreeswijk

The program consists of several packages, ordered in the following way:

```
nl
  .marcvanzee
    .KEProver
      .GUI    the graphical user interface. contains the file Main	
              RUN THIS FILE TO START THE PROGRAM
              
      .lib		a collection of static methods/variables
      
      .parser	reads the input and processes it
      .prover	the prover, SAKE in winKE sense.
```

When running "Main", a JApplet should load. This applet is pretty straightforward;
propositional logic formulas can be added on the left. When ready, the user can
load the collection of formulas into the prover. Once loaded, the button "prove"
will start the proving process. If the collection of formulas can be proven, the 
button "show proof" will enable, which will show the prove. Otherwise, a countermodel 
is shown directly.

For additional documentation, please refer to:
- [DESIGN.pdf](DESIGN.pdf): An overview of the design decisions made
- code: For detailed information on classes and methods
