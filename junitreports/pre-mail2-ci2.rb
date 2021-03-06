#!/opt/ruby-2.2.5/bin/ruby
require 'rubygems' # optional for Ruby 1.9 or above.
require 'premailer'

premailer = Premailer.new('report.html', :warn_level => Premailer::Warnings::SAFE)

# Write the HTML output
File.open("gmail_report.html", "w") do |fout|
  fout.puts premailer.to_inline_css
end

# Write the plain-text output
File.open("output.txt", "w") do |fout|
  fout.puts premailer.to_plain_text
end

# Output any CSS warnings
premailer.warnings.each do |w|
  puts "#{w[:message]} (#{w[:level]}) may not render properly in #{w[:clients]}"
end
