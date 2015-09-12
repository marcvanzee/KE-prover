package nl.marcvanzee.KEProver.GUI;

import javax.swing.*;

import nl.marcvanzee.KEProver.formula.Formula;
import nl.marcvanzee.KEProver.formula.Subformula;
import nl.marcvanzee.KEProver.parser.Parser;
import nl.marcvanzee.KEProver.prover.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
* The main class that runs the program as a JApplet.
* The FormulaEditor class is placed on a Container, and formulas are
* collected from this class when the button "load" is pressed
* 
* The resulting formula is first parsed, and then sent to the SAKE prover.
* SAKE.isProvable() will try to prove the formula when "prove" is pressed.
* 
* The resulting prove will be stored in the SAKE object, and requested when
* "show" proof is pressed.
* 
* @author Marc van Zee
*/
public class Main extends JApplet {
	private static final long serialVersionUID = 1L;

    private JButton btnRefute = new JButton("refute");
    private JButton btnLoad = new JButton("load");
    private JButton btnProofTree = new JButton("proof");
    
    private TextArea textarea = new TextArea(25,80);
    
    private Container topPane;
    private Container pane = new Container();
    private Container cntLeft = new Container();
    private Container cntLoad = new Container();
    private Container cntRight = new Container();
    private Container cntRightBottom = new Container();
    
    private FormulaEditor cntEditor = new FormulaEditor();
    
    private Formula formula;
    private Parser 	parser = new Parser();
    private SAKE 	sake = new SAKE();
        
    private Container drawPanel = new Container();
     
    /**
     * initialize the Applet. 
     * Disable buttons, place elements and add button listeners.
     */
    public void init(){
    	resize(1200,500);
    	topPane = getContentPane();
    	
    	cntEditor.setLoadButton(btnLoad);
    	    	
    	btnLoad.setEnabled(false);
    	btnRefute.setEnabled(false);
        btnProofTree.setEnabled(false);
        
    	addElements();
    	addButtonListeners();

        sake.setGui(this);
    }
    
    /**
     * Place elements on the Applet. Create a FormulaEditor object
     * and place it in a Container.
     */
    private void addElements() {
        cntLeft.setLayout(new BorderLayout());
        
        cntLeft.add(cntEditor, BorderLayout.NORTH);
        
        cntLoad.setLayout(new BorderLayout());
        cntLoad.add(Box.createHorizontalStrut(140), BorderLayout.WEST);
        cntLoad.add(btnLoad, BorderLayout.CENTER);
        cntLoad.add(Box.createHorizontalStrut(78), BorderLayout.EAST);
        
        cntLeft.add(cntLoad, BorderLayout.SOUTH);
        
        textarea.setFont(new Font("Verdana", Font.PLAIN, 12));
        textarea.setEditable(false);
        
        drawPanel.setLayout(new BorderLayout());
        drawPanel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        drawPanel.add(textarea, BorderLayout.SOUTH);

        cntRightBottom.setLayout(new BoxLayout(cntRightBottom, BoxLayout.X_AXIS));
        
        cntRightBottom.add(Box.createHorizontalStrut(260));
        cntRightBottom.add(btnRefute);
        cntRightBottom.add(Box.createHorizontalStrut(5));
        cntRightBottom.add(btnProofTree);
        
        cntRight.setLayout(new BorderLayout());
        cntRight.add(drawPanel, BorderLayout.NORTH);
        cntRight.add(cntRightBottom, BorderLayout.PAGE_END);
        cntRight.add(Box.createHorizontalStrut(5), BorderLayout.WEST);
        
        cntLeft.setPreferredSize(new Dimension(400, 320));
        pane.setLayout(new BorderLayout());
        pane.add(cntLeft, BorderLayout.WEST);
        pane.add(cntRight, BorderLayout.EAST);
        pane.add(Box.createHorizontalStrut(5), BorderLayout.CENTER);
        
        topPane.setLayout(new BorderLayout());
        topPane.add(pane, BorderLayout.WEST);
        topPane.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
    }
    
    /**
     * Add listeners for all the buttons.<br>
     * the load button retrieves all formulas from the FormulaEditor object when clicked,<br>
     * using the <code>getSubFormulas()</code> method, and then inserts the formula into the SAKE object.<br><br>
     * 
     * the refute button asks the SAKE object to prove the formulas. if it succeeds, the proof tree button is enabled.<br><br>
     * 
     * the proof tree button prints the proof
     */
    private void addButtonListeners() {
    	btnRefute.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		sake.insert(formula);
        		textarea.setText("");
        		if (sake.isProvable()) btnProofTree.setEnabled(true);        		
            }
        });
        
        btnProofTree.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {    			
        		textarea.setText("");
        		sake.printProofTree();
            }
        });

        btnLoad.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		formula = new Formula(getSubFormulas());
        		sake.insert(formula);
	        	
        		textarea.setText("formula loaded, press \"solve\" to start solving\n\n" + formula.toString());
        		
        		btnRefute.setEnabled(true);
        		btnProofTree.setEnabled(false);
            }
        });
    }
 
    /**
     * Collect the formulas from the listbox in FormulaEditor and
     * return them as a Vector of Subformulas. This is the input
     * format that SAKE requires.
     */
	private Vector <Subformula> getSubFormulas() {
		Subformula formula;
		DefaultListModel list = cntEditor.getListModel();
		
		Vector <Subformula> subformulas = new Vector <Subformula>();
		for (int i=0; i<list.size(); i++) {
			formula = parser.readLine(list.get(i).toString());
			subformulas.add(new Subformula(formula));
		}
		
		return subformulas;
	}
	
	/**
     * send a message to the textfield on the right side of the Applet.
     */
	public void msg(String str) {
		textarea.setText(textarea.getText()+ "\n" + str);
	}
}
