define pstack
  set $_addr = $esp
  set $_stopaddr = $esp + (4*$arg0)
  while ($_addr < $_stopaddr)
    set $_espoff = $_addr - $esp
    set $_ebpoff = $_addr - $ebp
    printf "esp+%02i 0x%08X 0x%08X ebp+%02i\n", $_espoff, $_addr, *(int*)$_addr, $_ebpoff
    set $_addr+=4
  end
end
