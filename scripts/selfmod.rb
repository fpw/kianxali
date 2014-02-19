# Iterates all instructions and displays write-access to code, i.e. finds self-modifying code
# Also tries to apply the changes statically to aid further analyzation

# Changed addresses will be remembered here in order to reanalyze them later
changes = []

$api.traverseCode do |inst|
  for op in inst.getDestOperands
    # Check if operand has code address as destination
    destAddr = op.asNumber
    next unless $api.isCodeAddress(destAddr)

    # Try patching by evaluating the modifying instruction    
    instAddr = inst.getMemAddress
    original = $api.readByte(destAddr)
    case inst.getMnemonic
    when "XOR"
      puts "#{instAddr.to_s(16)} modifies code at #{destAddr.to_s(16)} using XOR, patched"
      $api.patchByte(destAddr, original ^ inst.getSrcOperands.first.asNumber)
      changes << destAddr
    when "ADD"
      puts "#{instAddr.to_s(16)} modifies code at #{destAddr.to_s(16)} using ADD, patched"
      $api.patchByte(destAddr, original + inst.getSrcOperands.first.asNumber)
      changes << destAddr
    else
      puts "Unhandled mnemonic at #{instAddr.to_s(16)}: #{inst.getMnemonic}"
    end
  end
end

# Now reanalyze the changed addresses
changes.each {|dst| $api.reanalyze(dst)}

puts "#{changes.size} self-modifying instructions patched"
