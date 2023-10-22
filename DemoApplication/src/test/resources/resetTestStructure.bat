echo %~dp0
set current_dir = %~dp0
move /-y "%current_dir%out\0000001.vcf" "%current_dir%vcf\0000001.vcf"
move /-y "%current_dir%out\13665.vcf" "%current_dir%vcf\13665.vcf"
move /-y "%current_dir%out\0000001.hl7" "%current_dir%order\0000001.hl7"
move /-y "%current_dir%out\13665.hl7" "%current_dir%order\13665.hl7"
del "%current_dir%out\0000001_response.hl7"
del "%current_dir%out\13665_response.hl7"
del "%current_dir%out\temp\0000001.vcf.report.html"
del "%current_dir%out\temp\0000001.vcf.report.json"
del "%current_dir%out\temp\13665.vcf.report.html"
del "%current_dir%out\temp\13665.vcf.report.json"