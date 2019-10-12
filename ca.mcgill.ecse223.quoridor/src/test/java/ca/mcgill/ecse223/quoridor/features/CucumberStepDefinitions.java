package ca.mcgill.ecse223.quoridor.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.mcgill.ecse223.quoridor.QuoridorApplication;
import ca.mcgill.ecse223.quoridor.controller.QuoridorController;
import ca.mcgill.ecse223.quoridor.model.Board;
import ca.mcgill.ecse223.quoridor.model.Direction;
import ca.mcgill.ecse223.quoridor.model.Game;
import ca.mcgill.ecse223.quoridor.model.Game.GameStatus;
import ca.mcgill.ecse223.quoridor.model.Game.MoveMode;
import ca.mcgill.ecse223.quoridor.model.GamePosition;
import ca.mcgill.ecse223.quoridor.model.Player;
import ca.mcgill.ecse223.quoridor.model.PlayerPosition;
import ca.mcgill.ecse223.quoridor.model.Quoridor;
import ca.mcgill.ecse223.quoridor.model.Tile;
import ca.mcgill.ecse223.quoridor.model.User;
import ca.mcgill.ecse223.quoridor.model.Wall;
import ca.mcgill.ecse223.quoridor.model.WallMove;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CucumberStepDefinitions {

	// ***********************************************
	// Background step definitions
	// ***********************************************

	@Given("^The game is not running$")
	public void theGameIsNotRunning() {
		initQuoridorAndBoard();
		createUsersAndPlayers("user1", "user2");
	}

	@Given("^The game is running$")
	public void theGameIsRunning() {
		initQuoridorAndBoard();
		ArrayList<Player> createUsersAndPlayers = createUsersAndPlayers("user1", "user2");
		createAndStartGame(createUsersAndPlayers);
	}

	@And("^It is my turn to move$")
	public void itIsMyTurnToMove() throws Throwable {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		Player currentPlayer = quoridor.getCurrentGame().getWhitePlayer();
		QuoridorApplication.getQuoridor().getCurrentGame().getCurrentPosition().setPlayerToMove(currentPlayer);
	}

	@Given("The following walls exist:")
	public void theFollowingWallsExist(io.cucumber.datatable.DataTable dataTable) {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		List<Map<String, String>> valueMaps = dataTable.asMaps();
		// keys: wrow, wcol, wdir
		Player[] players = { quoridor.getCurrentGame().getWhitePlayer(), quoridor.getCurrentGame().getBlackPlayer() };
		int playerIdx = 0;
		int wallIdxForPlayer = 0;
		for (Map<String, String> map : valueMaps) {
			Integer wrow = Integer.decode(map.get("wrow"));
			Integer wcol = Integer.decode(map.get("wcol"));
			// Wall to place
			// Walls are placed on an alternating basis wrt. the owners
			//Wall wall = Wall.getWithId(playerIdx * 10 + wallIdxForPlayer);
			Wall wall = players[playerIdx].getWall(wallIdxForPlayer); // above implementation sets wall to null

			String dir = map.get("wdir");

			Direction direction;
			switch (dir) {
			case "horizontal":
				direction = Direction.Horizontal;
				break;
			case "vertical":
				direction = Direction.Vertical;
				break;
			default:
				throw new IllegalArgumentException("Unsupported wall direction was provided");
			}
			new WallMove(0, 1, players[playerIdx], quoridor.getBoard().getTile((wrow - 1) * 9 + wcol - 1), quoridor.getCurrentGame(), direction, wall);
			if (playerIdx == 0) {
				quoridor.getCurrentGame().getCurrentPosition().removeWhiteWallsInStock(wall);
				quoridor.getCurrentGame().getCurrentPosition().addWhiteWallsOnBoard(wall);
			} else {
				quoridor.getCurrentGame().getCurrentPosition().removeBlackWallsInStock(wall);
				quoridor.getCurrentGame().getCurrentPosition().addBlackWallsOnBoard(wall);
			}
			wallIdxForPlayer = wallIdxForPlayer + playerIdx;
			playerIdx++;
			playerIdx = playerIdx % 2;
		}
		System.out.println();

	}

	@And("I do not have a wall in my hand")
	public void iDoNotHaveAWallInMyHand() {
		// GUI-related feature -- TODO for later
	}
	
	@And("^I have a wall in my hand over the board$")
	public void iHaveAWallInMyHandOverTheBoard() throws Throwable {
		// GUI-related feature -- TODO for later
	}
	
	@Given("^A new game is initializing$")
	public void aNewGameIsInitializing() throws Throwable {
		initQuoridorAndBoard();
		ArrayList<Player> players = createUsersAndPlayers("user1", "user2");
		new Game(GameStatus.Initializing, MoveMode.PlayerMove, players.get(0), players.get(1), QuoridorApplication.getQuoridor());
	}

	// ***********************************************
	// Scenario and scenario outline step definitions
	// ***********************************************

	
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Sets up test preconditions such that the player whose turn it is to player is the provided player.
	 */
	@Given("The player to move is {string}")
	public void thePlayerToMoveIsPlayer(String playerColorAsString){
		//Complete the other player's turn. The method should not do anything if the other player's turn
		//is already "complete"; and if it is not complete, it will make it complete, thereby making it
		//the desired player's turn.
		Player providedPlayer = QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString);
		Player blackPlayer = QuoridorController.getCurrentBlackPlayer();
		Player whitePlayer = QuoridorController.getCurrentWhitePlayer();
		if( providedPlayer.equals(blackPlayer) ) {
			QuoridorController.completePlayerTurn(whitePlayer);	//If it's WhitePlayer's turn, end it. If not, nothing changes.
		} else {
			QuoridorController.completePlayerTurn(blackPlayer); //If it's BlackPlayer's turn, end it. If not, nothing happens.
		}
	}
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Sets up test preconditions such that the clock of the player whose turn it is to play is running
	 */
	@Given("The clock of {string} is running")
	public void theClockOfPlayerIsRunning(String playerColorAsString){
		QuoridorController.continuePlayerTimer( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) );
	}

	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Sets up test preconditions such that the clock of the player whose turn it is not to play is stopped
	 */
	@Given("The clock of {string} is stopped")
	public void theClockOfOtherIsStopped(String playerColorAsString){
		QuoridorController.stopPlayerTimer( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) );
	}
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Makes the selected player complete their move.
	 */
	@When("Player {string} completes his move")
	public void playerPlayerCompletesHisMove(String playerColorAsString){
		QuoridorController.completePlayerTurn( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) );
	}
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Asserts that the user interface now shows that is now the specified player's turn.
	 */
	@Then("The user interface shall be showing it is {string} turn")
	public void theUserInterfaceShallBeShowingItIsOtherTurn(String playerColorAsString){
		assertEquals( QuoridorController.getPlayerOfCurrentTurn().equals( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) ) , true );
	}
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Asserts that the user interface now shows that the specified player's timer is stopped.
	 */
	@Then("The clock of {string} shall be stopped")
	public void theClockOfPlayerShallBeStopped(String playerColorAsString){
		assertEquals( QuoridorController.getPlayerTimerRunning( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) ) , false );
	}
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Asserts that the user interface now shows that the specified player's timer is running.
	 */
	@Then("The clock of {string} shall be running")
	public void theClockOfOtherShallBeRunning(String playerColorAsString){
		assertEquals( QuoridorController.getPlayerTimerRunning( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) ) , true );
	}
	
	/**
	 * @author Edwin Pan
	 * @param player color in string - that is, a string describing the desired player's color as "black" or "white".
	 * Asserts that the user interface now shows that the next move belongs to the specified player.
	 */
	@Then("The next player to move shall be {string}")
	public void theNextPlayerToMoveShallBeOther(String playerColorAsString){
		assertEquals( QuoridorController.getPlayerOfCurrentTurn().equals( QuoridorController.getPlayerOfProvidedColorstring(playerColorAsString) ) , true );
	}
	
	
	

	// ***********************************************
	// Clean up
	// ***********************************************

	// After each scenario, the test model is discarded
	@After
	public void tearDown() {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		// Avoid null pointer for step definitions that are not yet implemented.
		if (quoridor != null) {
			quoridor.delete();
			quoridor = null;
		}
		for (int i = 0; i < 20; i++) {
			Wall wall = Wall.getWithId(i);
			if(wall != null) {
				wall.delete();
			}
		}
	}

	// ***********************************************
	// Extracted helper methods
	// ***********************************************

	// Place your extracted methods below

	private void initQuoridorAndBoard() {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		Board board = new Board(quoridor);
		// Creating tiles by rows, i.e., the column index changes with every tile
		// creation
		for (int i = 1; i <= 9; i++) { // rows
			for (int j = 1; j <= 9; j++) { // columns
				board.addTile(i, j);
			}
		}
	}

	private ArrayList<Player> createUsersAndPlayers(String userName1, String userName2) {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		User user1 = quoridor.addUser(userName1);
		User user2 = quoridor.addUser(userName2);

		int thinkingTime = 180;

		// Players are assumed to start on opposite sides and need to make progress
		// horizontally to get to the other side
		//@formatter:off
		/*
		 *  __________
		 * |          |
		 * |          |
		 * |x->    <-x|
		 * |          |
		 * |__________|
		 * 
		 */
		//@formatter:on
		Player player1 = new Player(new Time(thinkingTime), user1, 9, Direction.Horizontal);
		Player player2 = new Player(new Time(thinkingTime), user2, 1, Direction.Horizontal);

		Player[] players = { player1, player2 };

		// Create all walls. Walls with lower ID belong to player1,
		// while the second half belongs to player 2
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 10; j++) {
				new Wall(i * 10 + j, players[i]);
			}
		}
		
		ArrayList<Player> playersList = new ArrayList<Player>();
		playersList.add(player1);
		playersList.add(player2);
		
		return playersList;
	}

	private void createAndStartGame(ArrayList<Player> players) {
		Quoridor quoridor = QuoridorApplication.getQuoridor();
		// There are total 36 tiles in the first four rows and
		// indexing starts from 0 -> tiles with indices 36 and 36+8=44 are the starting
		// positions
		Tile player1StartPos = quoridor.getBoard().getTile(36);
		Tile player2StartPos = quoridor.getBoard().getTile(44);
		
		Game game = new Game(GameStatus.Running, MoveMode.PlayerMove, players.get(0), players.get(1), quoridor);

		PlayerPosition player1Position = new PlayerPosition(quoridor.getCurrentGame().getWhitePlayer(), player1StartPos);
		PlayerPosition player2Position = new PlayerPosition(quoridor.getCurrentGame().getBlackPlayer(), player2StartPos);

		GamePosition gamePosition = new GamePosition(0, player1Position, player2Position, players.get(0), game);

		// Add the walls as in stock for the players
		for (int j = 0; j < 10; j++) {
			Wall wall = Wall.getWithId(j);
			gamePosition.addWhiteWallsInStock(wall);
		}
		for (int j = 0; j < 10; j++) {
			Wall wall = Wall.getWithId(j + 10);
			gamePosition.addBlackWallsInStock(wall);
		}

		game.setCurrentPosition(gamePosition);
	}

}
