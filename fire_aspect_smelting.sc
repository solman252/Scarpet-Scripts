__config() -> {
  'stay_loaded' -> true,
};

get_smelt_result(item) -> (
  for(item_list(),
    recipe = recipe_data(_, 'smelting');
    if(recipe != null,
      for(recipe,
        if(_:1:0:0:0 == item,return(_:0:0););
      );
    );
  );
  return(null);
);

triangulate_drop(low, high, mode) -> (
  // Generate a random value between 0 and 1
  rand = rand(1);
  // Calculate the width of the distribution
  width = high - low;
  // If mode is provided and lies within the range, adjust the width
  if(low <= mode <= high,
    if(rand < (mode - low) / width,(
      return(round(low + (width * rand) ^ 0.5));
    ), //else
      return(round(high - (width * (1 - rand)) ^ 0.5));
    );
  );
  // Otherwise, use a linear interpolation
  return(round(low + rand * (high - low)));
);

global_fortune_chances = {
  'deepslate_iron_ore'->{
    'min'-> 1,
    0-> [1,1],
    1-> [2,1.33],
    2-> [3,1.75],
    3-> [4,2.2],
  },
  'deepslate_copper_ore'->{
    'min'-> 2,
    0-> [5,3.5],
    1-> [10,4.67],
    2-> [15,6.125],
    3-> [20,7.7],
  },
  'deepslate_gold_ore'->{
    'min'-> 1,
    0-> [1,1],
    1-> [2,1.33],
    2-> [3,1.75],
    3-> [4,2.2],
  },
  'iron_ore'->{
    'min'-> 1,
    0-> [1,1],
    1-> [2,1.33],
    2-> [3,1.75],
    3-> [4,2.2],
  },
  'copper_ore'->{
    'min'-> 2,
    0-> [5,3.5],
    1-> [10,4.67],
    2-> [15,6.125],
    3-> [20,7.7],
  },
  'gold_ore'->{
    'min'-> 1,
    0-> [1,1],
    1-> [2,1.33],
    2-> [3,1.75],
    3-> [4,2.2],
  }
  };

__on_player_breaks_block(player, block) -> (
  held_item = player ~ 'holds';
  if(held_item != null && str(held_item:0)~'sword' == null && player ~ 'gamemode' != 'creative',
    has_fire_aspect = __has_fire_aspect(held_item:2);
    if(has_fire_aspect == 1,(
      if(bool(rand(2)),
        if(get_smelt_result(block) != null,(
          count = 1;
          if(global_fortune_chances:str(block) != null,
            chances = global_fortune_chances:str(block);
            fortune_lvl = __has_fortune(held_item:2);
            if(fortune_lvl == null, fortune_lvl = 0);
            count = triangulate_drop(chances:'min',chances:fortune_lvl:0,chances:fortune_lvl:1)
          );
          spawn('item',pos(block)+[0.5,0,0.5],nbt('{Item:{id:'+get_smelt_result(block)+',Count:'+count+'}}'));
          set(pos(block),'air');
          return('cancel');
        ), //else
          return();
        );
      );
    ),//else if
    has_fire_aspect >= 2,
      if(get_smelt_result(block) != null,
        count = 1;
        if(global_fortune_chances:str(block) != null,
          chances = global_fortune_chances:str(block);
          fortune_lvl = __has_fortune(held_item:2);
          if(fortune_lvl == null, fortune_lvl = 0);
          count = triangulate_drop(chances:'min',chances:fortune_lvl:0,chances:fortune_lvl:1)
        );
        spawn('item',pos(block)+[0.5,0,0.5],nbt('{Item:{id:'+get_smelt_result(block)+',Count:'+count+'}}'));
        set(pos(block),'air');
        return('cancel');
      );
    );
  );
);

__has_fire_aspect(nbt) -> (
  enchants_list = parse_nbt(nbt):'Enchantments';
  if(enchants_list != null,
    first(enchants_list,
      _:'id' == 'minecraft:fire_aspect';
    ):'lvl',
  //else
    0
  );
);

__has_fortune(nbt) -> (
  enchants_list = parse_nbt(nbt):'Enchantments';
  if(enchants_list != null,
    first(enchants_list,
      _:'id' == 'minecraft:fortune';
    ):'lvl',
  //else
    0
  );
);