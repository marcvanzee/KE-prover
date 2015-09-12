package nl.marcvanzee.KEProver.GUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.marcvanzee.KEProver.formula.Subformula;
import nl.marcvanzee.KEProver.parser.Parser;

/**
 * The JApplet Panel where formulas can be entered by the user, and that will be added to the
 * formula list, if they are validated by the Parser.
 * 
 * @author marc
 *
 */
public class FormulaEditor extends JApplet implements ListSelectionListener {
	private static final long serialVersionUID = 1L;
	
	private Container cntConnectives = new Container();
	private Container pnlInput = new Container();
	private Container pnlFormulaButtons = new Container();
	
	private Container cntFormulas = new Container();
	private JButton btnNeg = new JButton("\u00ac");
    private JButton btnAnd = new JButton("&");
    private JButton btnOr = new JButton("v");
    private JButton btnImplies = new JButton("->");
    private JButton btnEquiv = new JButton("<->");
    
    private JButton btnAdd = new JButton("add");
    private JButton btnEdit = new JButton("edit");
    private JButton btnRemove = new JButton("remove");
    private JButton btnClear = new JButton("clear");

    // do not intialize this button yet, because it does not exist on this panel.
    // it will be intialized by a Main object, that we are an instance of, using the setLoadButton() method.
    private JButton btnLoad;
    
    private JTextField txtFormula = new JTextField(20);
    
    private JList list;
    private DefaultListModel listModel = new DefaultListModel();
    private JScrollPane listScrollPane;
    
	private Parser parser = new Parser();

	public FormulaEditor() {
		resize(300,325);
		txtFormula.setFont(new Font("Verdana", Font.PLAIN, 18));
		initList();
		
		addElements();
		addButtonListeners();
	}
	
	private void addElements() {
		cntConnectives.setLayout(new BoxLayout(cntConnectives, BoxLayout.X_AXIS));
		
		cntConnectives.add(Box.createHorizontalStrut(5)); 
		cntConnectives.add(Box.createHorizontalGlue()); 
    	cntConnectives.add(btnNeg);
    	cntConnectives.add(Box.createHorizontalGlue()); 
        cntConnectives.add(btnAnd);
        cntConnectives.add(Box.createHorizontalGlue());
        cntConnectives.add(btnOr);
        cntConnectives.add(Box.createHorizontalGlue());
        cntConnectives.add(btnImplies);
        cntConnectives.add(Box.createHorizontalGlue());
        cntConnectives.add(btnEquiv);
        cntConnectives.add(Box.createRigidArea(new Dimension(60, 0)));
        
        pnlInput.setLayout(new BorderLayout());
        pnlInput.add(Box.createHorizontalStrut(5), BorderLayout.WEST); 
        pnlInput.add(txtFormula, BorderLayout.CENTER);
        pnlInput.add(btnAdd, BorderLayout.EAST);
        pnlInput.add(Box.createVerticalStrut(5), BorderLayout.SOUTH); 
        
        listScrollPane = new JScrollPane(list);
        
        cntFormulas.setLayout(new BorderLayout());
        cntFormulas.add(Box.createHorizontalStrut(5), BorderLayout.WEST); 
        cntFormulas.add(listScrollPane, BorderLayout.CENTER);
        
        pnlFormulaButtons.setLayout(new BoxLayout(pnlFormulaButtons, BoxLayout.Y_AXIS));
        pnlFormulaButtons.add(btnEdit);
        pnlFormulaButtons.add(btnRemove);
        pnlFormulaButtons.add(btnClear);
        
        cntFormulas.add(pnlFormulaButtons, BorderLayout.EAST);   
        
        setLayout(new BorderLayout());
        cntConnectives.setPreferredSize(new Dimension(400,50));

        add(cntConnectives, BorderLayout.NORTH);
        add(pnlInput, BorderLayout.CENTER);
        add(cntFormulas, BorderLayout.SOUTH);
        
	}
	
	private void addButtonListeners() {
    	btnNeg.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		txtFormula.setText(txtFormula.getText() + "-");
        		txtFormula.requestFocus();
            }
        });

        btnAnd.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		txtFormula.setText(txtFormula.getText() + "&");
        		txtFormula.requestFocus();
            }
        });
        
        btnOr.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		txtFormula.setText(txtFormula.getText() + "|");
        		txtFormula.requestFocus();
            }
        });
        
        btnImplies.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		txtFormula.setText(txtFormula.getText() + ">");
        		txtFormula.requestFocus();
            }
        });
        
        btnEquiv.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		txtFormula.setText(txtFormula.getText() + "<");
        		txtFormula.requestFocus();
            }
        });
        
        /**
         * this is the place where the parser checks whether an input formula is valid. if so, it is added to the list on the GUI.
         */
        btnAdd.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		// first see if we have a valid formula
        		Subformula formula = parser.readLine(txtFormula.getText());
        		if (formula != null) {
        			listModel.add(listModel.size(), formula.print());
            		txtFormula.setText("");
            		btnLoad.setEnabled(true);
        		} else {
        			JOptionPane.showMessageDialog(getContentPane(), "Incorrect syntax",
		                       "ERROR in formula", JOptionPane.ERROR_MESSAGE );

        		}
        		txtFormula.requestFocus();
            }
        });
        
        btnEdit.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (!list.isSelectionEmpty()) {
        			txtFormula.setText(list.getSelectedValue().toString());
        			listModel.remove(list.getSelectedIndex());
        			if (listModel.size() == 0)
        				btnLoad.setEnabled(false);
        		}
        		txtFormula.requestFocus();
            }
        });
        
        btnRemove.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (!list.isSelectionEmpty()) {
        			listModel.remove(list.getSelectedIndex());
        			if (listModel.size() == 0)
        				btnLoad.setEnabled(false);
        		}
        		txtFormula.requestFocus();
            }
        });
        
        btnClear.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		listModel.clear();
        		txtFormula.requestFocus();
        		btnLoad.setEnabled(false);
            }
        });
    }
	
	private void initList() {
    	list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(15);
        list.setFixedCellWidth(50);

        list.setFont(new Font("Verdana", Font.PLAIN, 18));
    }
	
	public void setLoadButton(JButton btnLoad) {
		this.btnLoad = btnLoad;
	}
	
	public DefaultListModel getListModel() {
		return listModel;
	}
	
	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
