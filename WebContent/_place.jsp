<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="stratego.model.TileSymbol;"%>
<div id="gameContainer" class="classicclassic">
	<div class="panel panel-default panel-field">
		<div id="field" class="panel-body">
			<div class="tileRow">
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered"></div>
				<div class="tile tile-enemy_covered" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<!-- 
			//TEST CODE
			<div class="tileRow">
				<div class="tile tile-captain"></div>
				<div class="tile tile-sergeant"></div>
				<div class="tile tile-miner"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-spy"></div>
				<div class="tile tile-bomb"></div>
				<div class="tile tile-bomb"></div>
				<div class="tile tile-lieutenant"></div>
				<div class="tile tile-sergeant"></div>
				<div class="tile tile-captain" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-general"></div>
				<div class="tile tile-major"></div>
				<div class="tile tile-colonel"></div>
				<div class="tile tile-miner"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-bomb"></div>
				<div class="tile tile-bomb"></div>
				<div class="tile tile-miner"></div>
				<div class="tile tile-miner"></div>
				<div class="tile tile-lieutenant" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-colonel"></div>
				<div class="tile tile-major"></div>
				<div class="tile tile-lieutenant"></div>
				<div class="tile tile-sergeant"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-bomb"></div>
				<div class="tile tile-bomb"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-captain" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-marshall"></div>
				<div class="tile tile-major"></div>
				<div class="tile tile-sergeant"></div>
				<div class="tile tile-captain"></div>
				<div class="tile tile-miner"></div>
				<div class="tile tile-flag"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-scout"></div>
				<div class="tile tile-lieutenant" style="border-right:0"></div>
				<div class="clear"></div>
			</div>
			-->
			<div class="tileRow">
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="clear"></div>
			</div>
			<div class="tileRow">
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="tile tile-empty"></div>
				<div class="clear"></div>
			</div>
		</div>
	</div>
	
	<div class="panel panel-default">
		<div id="bank" class="panel-body">
			<div class="bankRow">
				<div class="bankTile tile-marshall"></div>
				<div class="bankTile tile-general"></div>
				<div class="bankSpacer">
					<button id="buttonSetStartPositions" class="btn btn-primary btn-lg btn-danger" role="button">Submit</button>
				</div>
				<div class="bankTile tile-colonel"></div>
				<div class="bankTile tile-major bankTile-last"></div>
				<div class="clear"></div>
			</div>
			<div class="bankRow">
				<div class="bankTile tile-captain"></div>
				<div class="bankTile tile-lieutenant"></div>
				<div class="bankTile tile-sergeant"></div>
				<div class="bankTile tile-miner"></div>
				<div class="bankTile tile-scout"></div>
				<div class="bankTile tile-spy"></div>
				<div class="bankTile tile-flag"></div>
				<div class="bankTile tile-bomb bankTile-last"></div>
				<div class="clear"></div>
			</div>
		</div>
	</div>
</div>