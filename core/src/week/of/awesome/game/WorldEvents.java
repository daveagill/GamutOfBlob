package week.of.awesome.game;

public interface WorldEvents {
	public void onBlobMoved(Tile tile);
	public void onCollectedGene();
	public void onCollectedStar();
	public void onLevelComplete();
	
	public void onButtonActivated();
	public void onButtonDeactivated();
}
