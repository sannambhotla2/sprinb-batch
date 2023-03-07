package com.infybuzz.config;

import java.io.File;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
//import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.infybuzz.model.StudentCsv;
import com.infybuzz.processor.FirstItemProcessor;
import com.infybuzz.writer.FirstItemWriter;
//import com.javatechie.spring.batch.config.CustomerProcessor;
//import com.javatechie.spring.batch.entity.Customer;
import com.javatechie.spring.batch.partition.ColumnRangePartitioner;

@Configuration
public class SampleJob {

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	
	@Autowired
	private FirstItemWriter firstItemWriter;

	 @Bean
	    public  FirstItemProcessor processor() {
	        return new  FirstItemProcessor();
	    }

	//reading data from csv file
	 
	public FlatFileItemReader<StudentCsv> flatFileItemReader() {
		FlatFileItemReader<StudentCsv> flatFileItemReader = 
				new FlatFileItemReader<StudentCsv>();
		
		//To set location of csv file
		
		flatFileItemReader.setResource(new FileSystemResource(
				new File("C:\\Users\\bnavyamadhuri\\Desktop\\Navya\\spring_batch\\people-1000.csv")));
		
		//To map data from csv file
		
		flatFileItemReader.setLineMapper(new DefaultLineMapper<StudentCsv>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames("ID", "First Name", "Last Name", "Email");
					}
				});
				
				setFieldSetMapper(new BeanWrapperFieldSetMapper<StudentCsv>() {
					{
						setTargetType(StudentCsv.class);
					}
				});
				
			}
		});
		
		flatFileItemReader.setLinesToSkip(1);
		
		return flatFileItemReader;
	}
	 //configuring column range partitioner
	  @Bean
	    public ColumnRangePartitioner partitioner() {
	        return new ColumnRangePartitioner();
	    }

	    // to set grid size to column range partitioner
	    @Bean
	    public PartitionHandler partitionHandler() {
	        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
	        taskExecutorPartitionHandler.setGridSize(2);
	        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
	        taskExecutorPartitionHandler.setStep(slaveStep());
	        return taskExecutorPartitionHandler;
	    }
	    
         // this step just performs  reading ,writing invoked by master step
	    @Bean
	    public Step slaveStep() {
	        return stepBuilderFactory.get("slaveStep").<StudentCsv, StudentCsv>chunk(500)
	                .reader(flatFileItemReader())
	                .processor(processor())
	                .writer(firstItemWriter)
	                .build();
	    }

	   
         // setting reference of partition handler.
		@Bean
	    public Step masterStep() {
	        return stepBuilderFactory.get("masterSTep").
	                partitioner(slaveStep().getName(), partitioner())
	                .partitionHandler(partitionHandler())
	                .build();
	    }
         
		// master step is given as input here
	    @Bean
	    public Job runJSob() {
	        return jobBuilderFactory.get("importCustomers")
	                .flow(masterStep()).end().build();

	    }

	    @Bean
	    public TaskExecutor taskExecutor() {
	        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	        taskExecutor.setMaxPoolSize(4);
	        taskExecutor.setCorePoolSize(4);
	        taskExecutor.setQueueCapacity(4);
	        return taskExecutor;
	    }
	    @StepScope
		@Bean
		public FlatFileItemWriter<StudentCsv> flatFileItemWriter(
				@Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
			FlatFileItemWriter<StudentCsv> flatFileItemWriter = 
					new FlatFileItemWriter<StudentCsv>();
			
			//flatFileItemWriter.setResource(fileSystemResource);
			flatFileItemWriter.setResource(new FileSystemResource(
					new File("C:\\Users\\bnavyamadhuri\\Desktop\\Navya\\spring_batch\\files\\Create-Flat-File-Item-Reader-with-CSV-File\\outputFiles\\output.csv")));

			
			flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentCsv>() {
				{
					setFieldExtractor(new BeanWrapperFieldExtractor<StudentCsv>() {
						{
							setNames(new String[] {"id", "firstName", "lastName", "email"});
						}
					});
				}
			});
			
			
			
			return flatFileItemWriter;
		}
}
