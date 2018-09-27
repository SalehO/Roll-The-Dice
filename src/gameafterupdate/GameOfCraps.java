package GameAfterUpdate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class GameOfCraps extends JFrame implements ActionListener {
	public static JLabel dice1Label;
	public static JLabel dice2Label;
	public static JLabel betAmountLabel;
	public static JLabel balanceLabel;
	
	public static JPanel gamePanel;
	
	public static int dice1Value;
	public static int dice2Value;
	public static int rerollTarget;
	public static int balanceLeft;
	public static int betAmount;
	
	public static ArrayList<Integer> rollValues;
	public static ArrayList<Transaction> transactions;
	
	public static JButton rollButton;
	public static JButton startGameButton;
	public static JButton depositCashButton;
	public static JButton transactionHistoryButton;
	
	
	public GameOfCraps() {
		this.setTitle("***** GAME *****");
		this.setLayout(new BorderLayout());
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		
		gamePanel = new JPanel();
		gamePanel.setLayout(null);
		gamePanel.setBackground(Color.white);
		add(BorderLayout.CENTER, gamePanel);
		
		dice1Label = new JLabel();
		dice2Label = new JLabel();
		
		dice1Label.setSize(85, 85);
		dice2Label.setSize(85, 85);
		
		gamePanel.add(this.dice1Label);
		gamePanel.add(this.dice2Label);
	
		rollButton = new JButton("Roll Dice");
		rollButton.addActionListener(this);
		rollButton.setEnabled(false);
		add(BorderLayout.NORTH, rollButton);
		
		rollValues = new ArrayList<Integer>();
		transactions = new ArrayList<Transaction>();
		
		JPanel optionsPanel = new JPanel(new FlowLayout());
		add(BorderLayout.SOUTH, optionsPanel);
		
		startGameButton = new JButton("START");
		depositCashButton = new JButton("Add Cash");
		transactionHistoryButton = new JButton("View Transactions");
		
		this.startGameButton.addActionListener(this);
		this.depositCashButton.addActionListener(this);
		this.transactionHistoryButton.addActionListener(this);
		
		betAmountLabel = new JLabel();
		balanceLabel = new JLabel("Balance: $0");
		
		optionsPanel.add(startGameButton);
		optionsPanel.add(depositCashButton);
		optionsPanel.add(transactionHistoryButton);
		optionsPanel.add(balanceLabel);
		optionsPanel.add(betAmountLabel);
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == this.startGameButton) {
			try {
				String betAmountTemp = JOptionPane.showInputDialog(this, "How much do you want to bet?");
				
				if(betAmountTemp == null)
					return;
				
				this.betAmount = Integer.parseInt(betAmountTemp);
				
				if(this.betAmount <= 0 || this.betAmount > this.balanceLeft) {
					JOptionPane.showMessageDialog(this, "Please enter an amount that is within your balance.");
					return;
				}
			} catch(Exception error) {
				JOptionPane.showMessageDialog(this, "You have entered an invalid value.");
				return;
			}
			
			this.betAmountLabel.setText("Bet Amount: $" + this.betAmount);
			
			this.rollButton.setEnabled(true);
			this.startGameButton.setEnabled(false);
			this.depositCashButton.setEnabled(false);
			
			return;
		}
		
		if(event.getSource() == this.depositCashButton) {
			try {
				String amountTemp = JOptionPane.showInputDialog(this, "How much do you want to deposit?");
				
				if(amountTemp == null)
					return;
				
				int amount = Integer.parseInt(amountTemp);
				
				if(amount <= 0) {
					JOptionPane.showMessageDialog(this, "Please enter a positive value.");
					return;
				}
				
				this.transactions.add(new Transaction("IN", amount));
				this.balanceLeft += amount;
				this.balanceLabel.setText("Balance: $" + this.balanceLeft);
			} catch(Exception error) {
				JOptionPane.showMessageDialog(this, "You have entered an invalid value.");
				return;
			}
			
			return;
		}
		
		if(event.getSource() == this.transactionHistoryButton) {
			JDialog dialog = new JDialog(this, true);
			dialog.setTitle("Transactions");
			dialog.setSize(400, 400);
			dialog.setLocationRelativeTo(this);
			
			JTable table = new JTable(new DefaultTableModel(new Object[] { "IN / OUT", "$" }, 0));
			
			for(Transaction transaction : this.transactions)
				((DefaultTableModel)table.getModel()).addRow(new Object[] { transaction.type, "$" + transaction.amount});
			
			dialog.setLayout(new BorderLayout());
			dialog.add(BorderLayout.CENTER, new JScrollPane(table));
			
			dialog.setVisible(true);
			
			return;
		}
		
		if(event.getActionCommand().equalsIgnoreCase("Roll Dice"))
			this.rollValues.clear();
		
		Thread thread = new Thread(new Animator());
		thread.start();
	}
	
	private class Animator implements Runnable {
		@Override
		public void run() {
			dice1Label.setLocation(0 - dice1Label.getWidth(), getHeight() / 2 - dice1Label.getHeight() / 2);
			dice2Label.setLocation(getWidth() + dice2Label.getWidth() - 30, getHeight() / 4 - dice2Label.getHeight() / 2);
			
			Random random = new Random();
			dice1Value = random.nextInt(6) + 1;
			dice2Value = random.nextInt(6) + 1;
			
			dice1Label.setIcon(new ImageIcon("dice" + dice1Value + ".jpg"));
                            dice2Label.setIcon(new ImageIcon("dice" + dice2Value + ".jpg"));
			
			dice1Label.updateUI();
			dice2Label.updateUI();
			
			gamePanel.updateUI();
			
			for(int i = 0; i < 50; i++) {
				dice1Label.setLocation(dice1Label.getX() + 5, dice1Label.getY());
				dice2Label.setLocation(dice2Label.getX() - 5, dice2Label.getY());
				
				try { Thread.sleep(10); } catch(Exception error) {}
				
				gamePanel.updateUI();
			}
			
			int diceSum = dice1Value + dice2Value;
			rollValues.add(diceSum);
			
			boolean finished = false;
			boolean win = false;
			
			if(rollButton.getText().equalsIgnoreCase("Roll Dice")) {
				if(diceSum == 7 || diceSum == 11) {
					JOptionPane.showMessageDialog(null, "You Win.\nYour rolls are: " + rollValues);
					finished = true;
					win = true;
				} else if(diceSum == 2 || diceSum == 3 || diceSum == 12) {
					JOptionPane.showMessageDialog(null, "You lose.\nYour rolls are: " + rollValues);
					finished = true;
				} else {
					JOptionPane.showMessageDialog(null, "Reroll the dice again.");
					rerollTarget = diceSum;
					rollButton.setText("Reroll Dice for " + rerollTarget);
				}
			} else {
				if(diceSum == 7) {
					JOptionPane.showMessageDialog(null, "You lose.\nYour rolls are: " + rollValues);
					rollButton.setText("Roll Dice");
					finished = true;
				} else if(diceSum == rerollTarget) {
					JOptionPane.showMessageDialog(null, "You Win.\nYour rolls are: " + rollValues);
					rollButton.setText("Roll Dice");
					win = true;
					finished = true;
				} else {
					JOptionPane.showMessageDialog(null, "Reroll the dice again.");
				}
			}
			
			if(finished) {
				if(win) {
					balanceLeft += betAmount;
					transactions.add(new Transaction("IN", betAmount));
					
					try {
			            Clip clip = AudioSystem.getClip();
			            clip.open(AudioSystem.getAudioInputStream(new File("greatjob.wav")));
			            clip.start();
			        } catch(Exception error) {
			            error.printStackTrace(System.out);
			        }
				} else {
					balanceLeft -= betAmount;
					transactions.add(new Transaction("OUT", betAmount));
					
					try {
			            Clip clip = AudioSystem.getClip();
			            clip.open(AudioSystem.getAudioInputStream(new File("sorry.wav")));
			            clip.start();
			        } catch(Exception error) {
			            error.printStackTrace(System.out);
			        }
				}
				
				rollButton.setEnabled(false);
				startGameButton.setEnabled(true);
				depositCashButton.setEnabled(true);
				betAmountLabel.setText("");
				balanceLabel.setText("Balance: $" + balanceLeft);
			}
		}
	}
	
	
}
