#!/usr/bin/env ruby
require 'rubygems'
require 'rexml/document'
require "rexml/streamlistener"

include REXML

class Parser
  attr_reader :tags
  include StreamListener

  def initialize
    @tags = []
  end
  
  def tag_start(element, attributes)
    @parse = true if element == 'mnem'
  end
  
  def text(text)
    @tags << text.gsub('.', '_') if @parse
  end
  
  def tag_end(element)
    @parse = false if element == 'mnem'
  end
end

ref = File.new "x86reference.xml"
parser = Parser.new
Document.parse_stream(ref, parser)
tags = parser.tags.uniq.sort

puts "// automatically generated, do not edit"
puts "package kianxali.cpu.x86;"
puts "public enum X86Mnemonic {"
puts tags.join(",\n")
puts "}"
