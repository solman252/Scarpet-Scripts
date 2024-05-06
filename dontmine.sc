// allows instamining deepslate if the player has an efficiency 5 netherite pick and haste 2
// works even if the player doesn't have carpet mod on their client

__config() -> {
  'stay_loaded' -> true,
  'commands' -> 
   {
      'toggle' -> _() ->  _toggle()
      
   }
};

global_enabled = false;

// list of blocks that can be instamined
// remove the comment below to allow cobbled deepslate, or add other blocks
global_blocks = [
  'suspicious_gravel',
  'suspicious_sand'
];


__on_player_clicks_block(player, block, face) -> (
  held_item = player ~ 'holds';
  if(held_item != null && global_enabled == true,
    if(global_blocks ~ str(block) != null && held_item:0 != 'brush',
      return('cancel')
    );
    [posx, posy, posz] = pos(block);
    if(['sand','gravel','suspicious_gravel','suspicious_sand'] ~ str(block(posx,posy+1,posz)),
      return('cancel')
    );
  );
);

_toggle() -> (
  global_enabled = !global_enabled;
)