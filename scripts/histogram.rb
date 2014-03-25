# Calculate a mnemonic histogram 

histogram = Hash.new(0)
total = 0

$api.traverseCode do |inst|
     mnem = inst.getMnemonic.to_s
     histogram[mnem] += 1
     total += 1
end

histogram.sort_by  {|mnem, count| count}.reverse_each do |a|
  puts "%10s: %5d (%.2f%%)" % [a[0], a[1], a[1] / total.to_f * 100]
end
