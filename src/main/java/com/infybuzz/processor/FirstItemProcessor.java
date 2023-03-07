package com.infybuzz.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.infybuzz.model.StudentCsv;

@Component
public class FirstItemProcessor implements ItemProcessor<StudentCsv,StudentCsv> {

	@Override
	public StudentCsv process(StudentCsv studentCsv) throws Exception {
		System.out.println("Inside Item Processor");
		return studentCsv;
	}

}
