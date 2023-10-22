echo %~dp0
set current_dir = %~dp0
move /-y "%current_dir%out\0000001.vcf" "%current_dir%vcf\0000001.vcf"
move /-y "%current_dir%out\0000002.vcf" "%current_dir%vcf\0000002.vcf"
move /-y "%current_dir%out\0000001.hl7" "%current_dir%orders\0000001.hl7"
move /-y "%current_dir%out\0000002.hl7" "%current_dir%orders\0000002.hl7"
del "%current_dir%out\0000001.vcf.report.html"
del "%current_dir%out\0000002.vcf.report.html"