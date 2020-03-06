import java.awt.Graphics;
import java.util.ArrayList;

import xobot.client.callback.listeners.MessageListener;
import xobot.client.callback.listeners.PaintListener;
import xobot.client.events.MessageEvent;
import xobot.script.ActiveScript;
import xobot.script.methods.Bank;
import xobot.script.methods.GameObjects;
import xobot.script.methods.GameTimers;
import xobot.script.methods.NPCs;
import xobot.script.methods.Packets;
import xobot.script.methods.Players;
import xobot.script.methods.Shop;
import xobot.script.methods.Walking;
import xobot.script.methods.Widgets;
import xobot.script.methods.tabs.Equipment;
import xobot.script.methods.tabs.Inventory;
import xobot.script.methods.tabs.Prayer.Prayers;
import xobot.script.methods.tabs.Skills;
import xobot.script.util.Time;
import xobot.script.util.Timer;
import xobot.script.wrappers.Area;
import xobot.script.wrappers.GameTimer;
import xobot.script.wrappers.Tile;
import xobot.script.wrappers.Widget;
import xobot.script.wrappers.interactive.Character;
import xobot.script.wrappers.interactive.GameObject;
import xobot.script.wrappers.interactive.Item;
import xobot.script.wrappers.interactive.NPC;
import xobot.script.wrappers.interactive.Player;

public class blackChins extends ActiveScript implements PaintListener, MessageListener {

	public int[] rangeItems = { 9185, 18357, 861, 868, 52804, 25037 };
	public int[] meleeItems = { 14484, 13899, 4675, 18353, 18351, 18349, 5698, 6528 };
	public int[] magicItems = { 15486, 4675, 18355, 1409, 42904, 19784};
	public ArrayList<Integer> rangeArray = new ArrayList<Integer>();
	public ArrayList<Integer> meleeArray = new ArrayList<Integer>();
	public ArrayList<Integer> mageArray = new ArrayList<Integer>();
	public int[] inventoryItems = { 10008, 385, 14415, 14128, 19189, 1712 };
	public Area chinArea1 = new Area(3140, 3814, 3143, 3817);
	public Area chinArea2 = new Area(3138, 3805, 3140, 3808);
	public Area chinArea3 = new Area(3138, 3818, 3142, 3821);
	public Area chinArea4 = new Area(3141, 3823, 3143, 3825);
	public int myChinArea;
	public Area myArea;
	private int boxID = 10008;
	private int sharkID = 385;
	private int restoreID = 14415;
	private int brewsID = 14128;
	private int gloryID = 1712;
	private int shakingID = 19189;
	private int numChins = 0;
	public Player attacker;
	public Timer t;

	public boolean onStart() {
		for (int i : rangeItems)
			rangeArray.add(i);
		for (int i : meleeItems)
			meleeArray.add(i);
		for (int i : magicItems)
			mageArray.add(i);
		Tile myTile = (Players.getMyPlayer().getLocation());
		if (chinArea1.contains(myTile))
			myArea = chinArea1;
		if (chinArea2.contains(myTile))
			myArea = chinArea2;
		if (chinArea3.contains(myTile))
			myArea = chinArea3;
		t = new Timer();
		return running;
	}

	public boolean atMarket() {
		Area market = new Area(3150, 3410, 3225, 3445);
		return market.contains(Players.getMyPlayer().getLocation());
	}

	public double getHealthPercent() {
		double result = (((double) Skills.CONSTITUTION.getCurrentLevel()) / (double) Skills.CONSTITUTION.getRealLevel())
				* 100.0;
		return result;
	}

	public boolean atEdge() {
		Area edge = new Area(3000, 3400, 3120, 3520);
		return edge.contains(Players.getMyPlayer().getLocation());
	}

	public boolean atChins() {
		Area chins = new Area(3050, 3600, 3180, 3880);
		return chins.contains(Players.getMyPlayer().getLocation());
	}

	public void buy(int id) {
		Shop.buy(id, 10);
	}

	public boolean isShopOpen() {
		return Widgets.getOpenInterface() == 3824;
	}

	public void buyChinLogic() {
		openShop(1866);
		Time.sleep(1000);
		if (isShopOpen()) {
			buy(boxID);
			Time.sleep(1250);
			withdrawPreset();
		}
	}

	public void buyGloryLogic() {
		openShop(519);
		Time.sleep(1000);
		if (isShopOpen()) {
			buy(1712);
			Time.sleep(1250);
			withdrawPreset();
		}
	}

	public void openShop(int id) {
		NPC shop = NPCs.getNearest(id);
		if (shop != null) {
			shop.interact("Talk-to");
			Time.sleep(() -> isShopOpen(), 5000);
		}
	}

	public int getChinStack() {
		int result = 0;
		Item chins = Inventory.getItem(41959);
		if (chins != null)
			result = chins.getStack();
		return result;
	}

	public void runSouth() {
		Tile currTile = Players.getMyPlayer().getLocation();
		Walking.walkTo(new Tile(currTile.getX(), currTile.getY() - 15));
	}

	public void escapeLogic() {
		runSouth();
		tankLogic();
		prayLogic();
		if (Players.getMyPlayer().getLocation().getY() < 3810)
			gloryTeleport();
		Prayers.AUGURY.Activate();
		Time.sleep(100);
	}

	public void withdrawPreset() {
		if (Skills.STRENGTH.getCurrentLevel() < Skills.STRENGTH.getRealLevel()
				|| (Skills.CONSTITUTION.getCurrentLevel() < Skills.CONSTITUTION.getRealLevel())) {
			restoreStats();
		}
		if (GameObjects.getAll() != null) {
			GameObject booth = GameObjects.getNearest(2213);
			if (booth != null) {
				booth.interact("View presets");
				Time.sleep(() -> Widgets.getOpenInterface() == 21350, 5000);
				Packets.sendAction("Select", null, 315, 0, 0, 21355);
				Time.sleep(() -> Widgets.getOpenInterface() == 21450, 5000);
				Packets.sendAction("Load this preset ", null, 315, 0, 0, 21455);
				Time.sleep(() -> Widgets.getOpenInterface() == -1, 5000);
			}
		}
		Time.sleep(1250);
		if (Inventory.getCount(10008) < 5)
			this.buyChinLogic();
		else if (!Equipment.containsOneOf(gloryID))
			this.buyGloryLogic();
		else if (Inventory.getCount(21630) < 1)
			this.buyRenewalLogic();
	}

	private void buyRenewalLogic() {
		openShop(278);
		Time.sleep(1000);
		if (isShopOpen()) {
			buy(21630);
			Time.sleep(1250);
			withdrawPreset();
		}

	}

	private void prayLogic() {
		try {
			if (this.attacker == null || this.attacker.equals(null) || !this.attacker.onMinimap()
					|| this.attacker.equals(Players.getMyPlayer())) {
				this.attacker = getAttacker();
			}
			if (attacker != null) {
				int[] array = attacker.getEquipment();
				if (array != null) {
					for (int i : array) {
						if (meleeArray.contains(i)) {
							if (!Prayers.PROTECT_FROM_MELEE.isActivated())
								prayMelee();
							return;
						} else if (rangeArray.contains(i)) {
							if (!Prayers.PROTECT_FROM_RANGE.isActivated())
								prayRange();
							return;
						} else if (mageArray.contains(i)) {
							if (!Prayers.PROTECT_FROM_MAGIC.isActivated())
								prayMage();
							return;
						}
					}
				}
				if (!Prayers.PROTECT_FROM_MAGIC.isActivated())
					prayMage();
				Prayers.AUGURY.Activate();
			}
		} catch (Exception e) {
			if (!Prayers.PROTECT_FROM_MAGIC.isActivated())
				prayMage();
		}

	}

	private void prayMage() {
		Prayers.PROTECT_FROM_MAGIC.Activate();

	}

	private void prayRange() {
		Prayers.PROTECT_FROM_RANGE.Activate();

	}

	private void prayMelee() {
		Prayers.PROTECT_FROM_MELEE.Activate();

	}

	public boolean lowHealth() {
		return Skills.CONSTITUTION.getRealLevel() - Skills.CONSTITUTION.getCurrentLevel() >= 10;
	}

	public void eatShark() {
		if (Inventory.getItems() != null) {
			Item shark = Inventory.getItem(385);
			if (shark != null) {
				shark.interact("Eat");
			}
		}
	}

	public void drinkBrew() {
		if (Inventory.getItems() != null) {
			if (Inventory.getAll(14128, 14126, 14124, 14122, 14419, 14417) != null)
				for (Item i : Inventory.getAll(14128, 14126, 14124, 14122, 14419, 14417)) {
					if (i != null) {
						i.interact("Drink");
						Time.sleep(200);
						return;
					}
				}
		}
	}

	public void drinkRenewal() {
		if (Inventory.getItems() != null) {
			if (Inventory.getAll(21630, 21632, 21634, 21636, 15119, 15121, 14289, 14287, 15123, 15117) != null)
				for (Item i : Inventory.getAll(21630, 21632, 21634, 21636, 15119, 15121, 14289, 14287, 15123, 15117)) {
					if (i != null) {
						i.interact("Drink");
						Time.sleep(200);
						return;
					}
				}
		}
	}

	public boolean containsBrew() {
		return Inventory.Contains(14128, 14126, 14124, 14122, 14419, 14417);
	}

	public void drinkRestore() {
		if (Inventory.getItems() != null) {
			if (Inventory.getAll(14415, 14413, 14411, 14409, 14407, 14405) != null)
				for (Item i : Inventory.getAll(14415, 14413, 14411, 14409, 14407, 14405)) {
					if (i != null) {
						i.interact("Drink");
						Time.sleep(200);
						return;
					}
				}
		}
	}

	public void tankLogic() {
		if (lowHealth()) {
			eatShark();
			drinkBrew();
		}
		if (veryLowHealth()) {
			eatShark();
			drinkBrew();
		}
		if (Skills.PRAYER.getCurrentLevel() <= 30) {
			drinkRestore();
		}

	}

	public void openBank() {
		GameObject bank = GameObjects.getNearest(2213);
		if (bank != null) {
			bank.interact("Use");
			Time.sleep(() -> isBankOpen(), 5000);
			Time.sleep(1000);
		}
	}

	public void closeBank() {
		if (isBankOpen()) {
			Packets.sendAction("Close", "", 200, 0, 0, 5384);
			Time.sleep(() -> !isBankOpen(), 5000);
		}
	}

	public void bankGrabInventory() {
		Bank.depositAll();
		for (int i : inventoryItems) {
			if (i == brewsID) {
				Bank.withdraw(brewsID, 10);
			} else if (i == restoreID) {
				Bank.withdraw(restoreID, 5);
			} else if (i == boxID) {
				Bank.withdraw(boxID, 5);
			} else if (i == sharkID) {
				Bank.withdraw(sharkID, 5);
			} else if (i == 1712) {
				Bank.withdraw(i, 1);
			}
			Time.sleep(200);
		}
		closeBank();
		Time.sleep(650);
		if (Inventory.Contains(1712)) {
			Inventory.getItem(1712).interact("Wear");
			Time.sleep(2000);
		}
	}

	public boolean isAttackerValid() {
		if (attacker == null) {
			return false;
		}
		if (attacker != null && !attacker.equals(Players.getMyPlayer())) {
			Character c = attacker.getInteractingCharacter();
			if (c != null) {
				if (c.equals(Players.getMyPlayer()))
					return true;
			}
		}
		return false;
	}

	public boolean doneBanking() {
		return Equipment.containsOneOf(gloryID) && Inventory.getCount(brewsID) >= 5 && Inventory.getCount(boxID) >= 5
				&& Inventory.getCount(21630) >= 1;
	}

	public boolean isBankOpen() {
		return Widgets.getOpenInterface() == 5292;
	}

	public boolean isUnderAttack() {
		return Players.getMyPlayer().isInCombat();
	}

	public boolean isBeingAttacked() {
		Player[] array = Players.getAll();
		if (array != null) {
			for (Player p : array) {
				if (p != null) {
					Character c = p.getInteractingCharacter();
					if (c != null) {
						if (c.equals(Players.getMyPlayer()) && Players.getMyPlayer().isInCombat())
							return true;
					}
				}
			}
			return false;
		}
		return false;
	}

	public Player getAttacker() {
		Player[] array = Players.getAll();
		if (array != null) {
			for (Player p : array) {
				if (p != null) {
					Character c = p.getInteractingCharacter();
					if (c != null && c.isValid()) {
						if (c.equals(Players.getMyPlayer()))
							return p;
					}
				}
			}
			return Players.getMyPlayer();
		}
		return Players.getMyPlayer();
	}

	public void restoreStats() {
		GameObject box = GameObjects.getNearest(16118);
		if (box != null) {
			box.interact("Use");
			Time.sleep(() -> box.getDistance() <= 0, 5000);
			Time.sleep(600);
		}
	}
	

	public void teleportChins() {
		Packets.sendAction("Cast<col=65280>Skill teleports", null, 315, 0, 0, 1541);
		Time.sleep(100);
		Packets.sendAction("Ok", null, 315, 0, 0, 2498);
		Time.sleep(100);
		Packets.sendAction("Ok", null, 315, 0, 0, 2498);
		Time.sleep(100);
		Packets.sendAction("Ok", null, 315, 0, 0, 2498);
		Time.sleep(100);
		Packets.sendAction("Ok", null, 315, 0, 0, 2496);
		Time.sleep(100);
		Packets.sendAction("Ok", null, 315, 0, 0, 2498);
		Time.sleep(100);
		Packets.sendAction("Ok", null, 315, 0, 0, 2496);

	}

	public void gloryTeleport() {
		Packets.sendAction("Edgeville", "<col=ff9040>Amulet of glory(4)", 432, 1712, 2, 1688);
		Time.sleep(100);
	}

	public void initialLayTrap() {
		Item i = Inventory.getItem(boxID);
		if (i != null) {
			Tile dropTile = (myArea.getCentralTile().randomize(1, 1));
			if (dropTile.getLocation().getDistance() <= 10) {
				GameObject check = GameObjects.getNearest(
						a -> a.getLocation().equals(dropTile) && (a.getId() == 19187 || a.getId() == shakingID));
				if (check == null) {
					Walking.walkTo(dropTile);
					Time.sleep(() -> dropTile.getDistance() == 0, 5000);
					Time.sleep(100);
					i.interact("Lay");
					Time.sleep(()->isUnderAttack(),1100);
				}
			}
		}
	}


	public void pickupTrap() {
		GameObject[] boxes = GameObjects.getAll(e -> e.getId() == shakingID && myArea.contains(e.getLocation()));
		if (boxes != null) {
			for (GameObject g : boxes) {
				g.interact("Check");
				Time.sleep(() -> g.getDistance() <= 1 || isUnderAttack(), 2250);
				if (isAttackerValid())
					break;
				Time.sleep(800, 1200);
				

			}
		}
	}

	public boolean veryLowHealth() {
		return (Skills.CONSTITUTION.getRealLevel() - Skills.CONSTITUTION.getCurrentLevel()) >= 25;
	}

	public void determineMyArea() {
		int n1 = countBoxes(chinArea1);
		int n2 = countBoxes(chinArea2);
		int n3 = countBoxes(chinArea3);
		int n4 = countBoxes(chinArea4);
		int res = Math.min(n1, Math.min(n2, Math.min(n3, n4)));
		if (res == n1)
			myArea = chinArea1;
		else if (res == n2)
			myArea = chinArea2;
		else if (res == n3)
			myArea = chinArea3;
		else if(res == n4)
			myArea = chinArea4;

	}

	public int countBoxes(Area a) {
		int result = 0;
		for (GameObject g : GameObjects
				.getAll(e -> a.contains(e.getLocation()) && ((e.getId() == 19187 || e.getId() == shakingID)))) {
			result++;
		}
		return result;
	}

	public boolean hasBoxes() {
		return Inventory.getCount(boxID) > 0;
	}

	public boolean outofsharks() {
		return !Inventory.Contains(385);
	}

	public void leaveChins() {
		this.runSouth();
		Time.sleep(250);
		if (Players.getMyPlayer().getLocation().getY() <= 3775) {
			this.gloryTeleport();
			Time.sleep(250);
		}
	}

	public void unactivatePrayers() {
		Prayers.AUGURY.deActivate();
		Prayers.PROTECT_FROM_MAGIC.deActivate();
		Prayers.PROTECT_FROM_MELEE.deActivate();
		Prayers.PROTECT_FROM_RANGE.deActivate();
	}

	public boolean doneRenewal() {
		GameTimer dose4 = GameTimers.getTimer(21630);
		GameTimer dose3 = GameTimers.getTimer(21632);
		GameTimer dose2 = GameTimers.getTimer(21634);
		GameTimer dose1 = GameTimers.getTimer(21636);
		return (dose4 == null && dose3 == null && dose2 == null && dose1 == null);
	}

	public int getY() {
		return Players.getMyPlayer().getLocation().getY();
	}
	
	public int getWildyLevel(){
		Widget root = Widgets.get(200);
		if (root != null && root.getChild(1) != null) {
			String special = root.getChild(11).getChildren()[22].getText();
			String[] x = special.split(": ");
			String r = x[1];
			String res = r.replaceAll(" ", "");
			return Integer.parseInt(res);

		}
		return 0;
	}

	public void logic() {
		if ((atEdge() || atMarket()) && doneBanking()) {
			drinkRenewal();
			Prayers.PROTECT_FROM_MAGIC.Activate();
			this.teleportChins();
			Time.sleep(3000, 4000);
			if (atChins()) {
				determineMyArea();
				Walking.walkTo(myArea.getCentralTile());
			}
		}
		if ((atEdge() || atMarket()) && !doneBanking() && !isBankOpen()) {
			withdrawPreset();
		}
		if (atChins()) {
			chinsLogic();
		}
		if (!atChins())
			unactivatePrayers();
	}

	public void chinsLogic() {
		if (myArea == null)
			determineMyArea();
		if (isBeingAttacked())
			prayLogic();
		if (isBeingAttacked() && outofsharks())
			leaveChins();
		if (catchCondition())
			pickupTrap();
		if (catchCondition())
			initialLayTrap();
		if (lowHealth())
			tankLogic();
		if (lowPrayer())
			drinkRestore();
		if (!isUnderAttack() && Players.getMyPlayer().getLocation().getY() <= 3790)
			leaveChins();
		if (this.getChinStack() >= 50 || Players.getMyPlayer().getLocation().getY() <= 3800)
			leaveChins();
		if (dropCondition())
			dropBoxes();
	}

	public boolean dropCondition() {
		return Inventory.getCount(boxID) > 5;
	}

	public void dropBoxes() {
		Item box = Inventory.getItem(boxID);
		if (box != null) {
			box.interact("Drop");
			Time.sleep(300, 600);
		}
	}

	public boolean catchCondition() {
		return (getChinStack() < 50 && !outofsharks());
	}

	private boolean lowPrayer() {
		return Skills.PRAYER.getCurrentLevel() <= 20;
	}

	@Override
	public void MessageRecieved(MessageEvent arg0) {
		if (arg0.getMessage().contains("Black chinchompa!")) {
			numChins++;
		}
		if (arg0.getMessage().contains("has run out")) {
			drinkRenewal();
		}

	}

	@Override
	public void repaint(Graphics g) {
		g.drawString("Chins caught: " + numChins, 20, 100);
		g.drawString("profit: " + (413 * numChins) + "k", 20, 120);
		g.drawString("Time: " + t.toElapsedString(), 20, 140);
		//g.drawString("Wildy leve: " + getWildyLevel(), 20, 160);
		/*
		 * if (atChins()) { g.drawString("Boxes area 1: " +
		 * countBoxes(this.chinArea1), 20, 160); g.drawString("Boxes area 2: " +
		 * countBoxes(this.chinArea2), 20, 180); g.drawString("Boxes area 3: " +
		 * countBoxes(this.chinArea3), 20, 200); }
		 */

	}

	@Override
	public int loop() {
		logic();
		return 30;
	}

}
