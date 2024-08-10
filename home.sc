__config() -> {
  'stay_loaded' -> true,
  'legacy_command_type_support' -> true,
  'commands' -> {
    '' -> '_command'
  }
};

global_enabled = true;

_command() -> (
  [loc, dim, rot, forced] = query(player(), 'spawn_point');
  print('Welcome home :)');
  modify(player(), 'pos', loc);
);