__config() -> {
  'stay_loaded' -> true,
  'commands' -> 
   {
      'toggle' -> _() ->  _toggle()
      
   }
};

global_enabled = true;

_toggle() -> (
  global_enabled = !global_enabled;
  print('easy move beacon enabled set to '+global_enabled)
);

// beacon_base_blocks
global_blocks = [
    'copper_block',
    'exposed_copper',
    'weathered_copper',
    'oxidized_copper',
    'waxed_copper_block',
    'waxed_exposed_copper',
    'waxed_weathered_copper',
    'waxed_oxidized_copper',
    'gold_block',
    'iron_block',
    'emerald_block',
    'diamond_block',
    'netherite_block'
];

get_layers(beacon) -> (
  beacon_pos = pos(beacon);
  valid_layers = 0;
  loop(4,
    layer = _+1;
    loop(2*layer+1,
      i = _-layer;
      loop(2*layer+1,
        if(global_blocks~block(beacon_pos - [i,layer,_-layer]) == null,
          return(valid_layers);
        );
      );
    );
    valid_layers += 1;
  );
  return(valid_layers);
);

get_blocks(beacon,layers) -> (
  beacon_pos = pos(beacon);
  items = [];
  loop(layers,
    layer = _+1;
    loop(2*layer+1,
      i = _-layer;
      loop(2*layer+1,
        item = block(beacon_pos - [i,layer,_-layer]);
        items += str(item);
        set(pos(item),'air');
      );
    );
  );
  return(items);
);

occurances(input_list) -> (
  count_dict = {};
  for(input_list,
    if(count_dict~_ != null,(
      count_dict:_ += 1;
    ), // else
      count_dict:_ = 1;
    );
  );
  return(count_dict);
);

stacks(frequency_dict) -> (
    formatted_dict = {};
    for(frequency_dict,
        key = _;
        value = frequency_dict:key;
        group_list = [];
        while(value > 0,
            if(value <= 64,(
              group_list += value;
            ), // else
              group_list += 64;
            );
            value = value-64;
        );
        formatted_dict:key = group_list;
    );
    return(formatted_dict);
);

__on_player_breaks_block(player, block) -> (
  if(global_enabled == true && block == 'beacon' && player ~ 'sneaking',
    layers = get_layers(block);
    items = get_blocks(block,layers);
    items = occurances(items);
    items = stacks(items);
    if(player~'gamemode' != 'creative',
      for(items,
        key = _;
        value = items:_;
        for(value,
          spawn('item',pos(block)-[-0.5,layers,-0.5],nbt('{Item:{id:"minecraft:'+key+'",Count:'+_+'}}'));
        )
      );
      spawn('item',pos(block)-[-0.5,layers,-0.5],nbt('{Item:{id:"minecraft:beacon",Count:1}}'));
    );
    set(pos(block),'air');
    run('tp '+player+' '+pos(block):0+' '+(pos(block):1-layers)+' '+pos(block):2);
    return('cancel');
  );
);

count_total_in_inventory(player) -> (
  total = 0;
  for(range(0,41),
    tuple = inventory_get(player,_);
    if((global_blocks ~ str(tuple:0)) != null,
      total += tuple:1;
    );
  );
  return(total);
);

remove_from_inventory(player) -> (
  for(range(0,41),
    tuple = inventory_get(player,_);
    if(global_blocks ~ str(tuple:0) != null,
      if(player~'gamemode' != 'creative',
        inventory_set(player,_,tuple:1-1);
      );
      return(tuple:0);
    );
  );
);

place_layer(beacon,player,layer,total_layers) -> (
  beacon_pos = pos(beacon)+[0,total_layers-layer,0];
  loop(2*layer+1,
    i = _-layer;
    loop(2*layer+1,
      set(beacon_pos - [i,0,_-layer],remove_from_inventory(player));
    );
  );
);

area_clear(beacon,total_layers) -> (
  beacon_pos = pos(beacon);
  valid_layers = 0;
  loop(total_layers,
    layer = _+1;
    loop(2*layer+1,
      i = _-layer;
      loop(2*layer+1,
        pos = beacon_pos - [i,(total_layers-layer)*-1,_-layer];
        if(['air','water','lava']~str(block(pos)) == null,
          return(valid_layers);
        );
      );
    );
    valid_layers += 1;
  );
  return(valid_layers);
);

__on_player_placing_block(player, item_tuple, hand, block) -> (
  if(global_enabled == true && item_tuple:0 == 'beacon' && player ~ 'sneaking',
    total = count_total_in_inventory(player);
    layers = [];
    if(total >= 9,
      layers += 1;
    );
    if(total >= 34,
      layers += 2;
    );
    if(total >= 83,
      layers += 3;
    );
    if(total >= 164,
      layers += 4;
    );
    if(area_clear(block,length(layers)) < length(layers),
      print('Something is blocking the way.');
      return('cancel');
    );
    for(layers,
      place_layer(block,player,_,length(layers));
    );
    set(pos(block)+[0,length(layers),0],'beacon');
    if(player~'gamemode' != 'creative',
      if(hand == 'mainhand',(
        inventory_set(player,player~'selected_slot',(inventory_get(player,player~'selected_slot'):1)-1);
      ), // else
        inventory_set(player,40,(inventory_get(player,40):1)-1);
      );
    );
    if(length(layers) > 0,
      run('tp '+player+' '+pos(block):0+' '+(pos(block):1+length(layers)+1)+' '+pos(block):2);
      return('cancel');
    );
  );
);