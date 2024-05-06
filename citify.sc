__config() -> {
   'commands' -> 
   {
      'get' -> _() ->  _get(),
      'set <id>' -> '_set',
      'reset' -> _() ->  _reset()
      
   },
   'arguments' -> {
      'id' -> {'type' -> 'string', 'suggest' -> ['"CIT ID"']},
   }
};

_set(cit_id) -> (
    slot = player()~'selected_slot';
    item = inventory_get(player(), slot);
    if( item == null, (
        print(format('r You are not holding anything.'))
    ) // else
        [id,count,nbt] = item;
        if( nbt == null,
            nbt = nbt('{}')
        );
        put(nbt,'CIT','"'+cit_id+'"');
        inventory_set(player(), slot,count,id,nbt)
    );
);
_reset() -> (
    slot = player()~'selected_slot';
    item = inventory_get(player(), slot);
    if( item == null, (
        print(format('r You are not holding anything.'))
    ), // else
        [id,count,nbt] = item;
        if( nbt:'CIT' == null, (
            print(format('r The item you are holding is not a CIT item.'))
        ), // else
            delete(nbt,'CIT');
            inventory_set(player(), slot,count,id,nbt)
            
        );
        
    );
);
_get() -> (
    slot = player()~'selected_slot';
    item = inventory_get(player(), slot);
    if( item == null, (
        print(format('r You are not holding anything.'))
    ), // else
        [id,count,nbt] = item;
        if( nbt:'CIT' == null, (
            print(format('r The item you are holding is not a CIT item.'))
        ), // else
            print(format('w The CIT-ID of the item you are holding is:\n  ','lb \'\''+nbt:'CIT'+'\'\''))
        );
        
    );
);

__on_player_right_clicks_block(player, item, hand, block, face, h) -> (
	if(
		player()~'sneaking' && 
        item:2:'CIT' != null &&
		block != 'air',
		slot = if(hand=='mainhand', player()~'selected_slot', -1);
        if(
            player()~'gamemode_id'%2==0, 
                inventory_set(player(), slot, item:1 - 1);
            );
        if( item:2 == null,
		    item:2 = nbt('{}')
        );
        if(
        face == 'south', (
            facing = 3
        ), // else if
        face == 'west', (
            facing = 4
        ), // else if
        face == 'north', (
            facing = 2
        ), // else if
        face == 'east', (
            facing = 5
        ), // else if
        face == 'up', (
            facing = 1
        ), // else if
        face == 'down', (
            facing = 0
        ));

        yaw = query(player(), 'yaw');
        rounded_yaw = round(yaw / 45)*45;
        if( face != 'up', (
            rot = 0;
        ), (
            if(
                rounded_yaw == -90, (
                    rot = 2;
                ), // else if
                rounded_yaw == -45, (
                    rot = 3;
                ), // else if
                rounded_yaw == 0, (
                    rot = 4;
                ), // else if
                rounded_yaw == 45, (
                    rot = 5;
                ), // else if
                rounded_yaw == 90, (
                    rot = 6;
                ), // else if
                rounded_yaw == 135, (
                    rot = 7;
                ), // else if
                rounded_yaw == 180 || rounded_yaw == -180, (
                    rot = 0;
                ), // else if
                rounded_yaw == -135, (
                    rot = 1;
                ));
        ));

        spawn('item_frame',pos_offset(pos(block),face,1),nbt('{Tags:["CITPLACED"],Facing:'+facing+',Fixed:1,Invisible:1,ItemRotation:'+rot+'b,Item:{id:"minecraft:'+item:0+'",Count:1,tag:'+item:2+'}}'));
        return('cancel')
	)
);
__on_player_attacks_entity(player, entity) -> (
    if(entity~'tags'~'CITPLACED' != null && player()~'gamemode_id'%2==0,
        [id,count,tag] = entity~'item';
        spawn('item',pos(entity),nbt('{Item:{id:"minecraft:'+id+'",Count:'+count+',tag:'+tag+'}}'));
        modify(entity, 'remove');
        return('cancel')
    );
);
__on_player_interacts_with_entity(player, entity, hand) -> (
    if(entity~'tags'~'CITPLACED' != null && player()~'sneaking',
        rot = entity~'nbt':'ItemRotation';
        rot = rot + 1;
        if(rot == 8, rot = 0);
        modify(entity, 'nbt_merge', '{ItemRotation:'+rot+'}');
        return('cancel')
    );
);