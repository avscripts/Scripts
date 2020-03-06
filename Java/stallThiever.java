import java.awt.Graphics;

import xobot.client.callback.listeners.MessageListener;
import xobot.client.callback.listeners.PaintListener;
import xobot.client.events.MessageEvent;
import xobot.script.ActiveScript;
import xobot.script.Manifest;
import xobot.script.methods.GameObjects;
import xobot.script.methods.NPCs;
import xobot.script.methods.Packets;
import xobot.script.methods.Players;
import xobot.script.methods.Widgets;
import xobot.script.methods.tabs.Inventory;
import xobot.script.methods.tabs.Skills;
import xobot.script.util.Time;
import xobot.script.wrappers.Area;
import xobot.script.wrappers.Tile;
import xobot.script.wrappers.interactive.GameObject;
import xobot.script.wrappers.interactive.Item;
import xobot.script.wrappers.interactive.NPC;

public class stallThiever  extends ActiveScript implements PaintListener, MessageListener {
	public int[] items = {1635,950,1331,1391,1639};
	
	public void sellItems(){
		if(Inventory.getItems() != null){
			for(int i : items){
				Item sellItem = Inventory.getItem(i);
				if(sellItem!=null){
					sellItem.interact("Sell 10");
					Time.sleep(500);
					sellItem.interact("Sell 10");
					sellItem.interact("Sell 10");
					Time.sleep(500);
				}
			}
		}
	}
	
	public boolean isShopOpen(){
		return Widgets.getOpenInterface() == 3824;
	}
	
	public boolean doneShop(){
		return isShopOpen() && !Inventory.Contains(items);
	}
	public void openShop(){
		NPC bandit = NPCs.getNearest(1878);
		if(bandit!=null){
			bandit.interact("Talk-to");
			Time.sleep(()-> isShopOpen(), 5000);
		}
	}
	public void thieve(){
		int thieveLevel = Skills.THIEVING.getCurrentLevel();
		GameObject stall = null;
		if(thieveLevel <= 30) stall = GameObjects.getNearest(4875);
		if(thieveLevel >= 30 && thieveLevel < 60)stall = GameObjects.getNearest(4874);
		if(thieveLevel>= 60 && thieveLevel<  65)stall = GameObjects.getNearest(4876);
		if(thieveLevel>= 65  && thieveLevel < 80)stall = GameObjects.getNearest(4877);
		if(thieveLevel >= 80)stall = GameObjects.getNearest(4878);
		if(stall!=null){
			stall.interact("Steal from");
			Time.sleep(800,1200);
		}

		
	}
	
	public boolean inLumbridge(){
		Area lumby = new Area(new Tile(3150,3150), new Tile(3250,3250));
		return lumby.contains(Players.getMyPlayer().getLocation());
	}
	
	public void teleportHome(){
		Packets.sendAction("Cast<col=65280>Home teleport", null,646,0, 0, 1195);
	}
	
	public void logic(){
		if(inLumbridge()) teleportHome();
		if(isShopOpen())sellItems();
		if(Inventory.getCount() == 28) openShop();
		if(doneShop()) thieve();
		if(Inventory.getCount() < 28 && !inLumbridge()) thieve();
	}
	@Override
	public void MessageRecieved(MessageEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repaint(Graphics g) {
		
	}

	@Override
	public int loop() {
		logic();
		return 100;
	}

}
