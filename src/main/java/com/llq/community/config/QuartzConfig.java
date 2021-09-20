package com.llq.community.config;

import com.llq.community.quartz.AlphaJob;
import com.llq.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {

    // FactoryBean可简化Bean的实例化过程:
    // 1.通过FactoryBean封装Bean的实例化过程.
    // 2.将FactoryBean装配到Spring容器里.
    // 3.将FactoryBean注入给其他的Bean.
    // 4.该Bean得到的是FactoryBean所管理的对象实例.

    // 配置JobDetail
    //@Bean   //创建一个对象，并将这个对象放入spring容器中
    public JobDetailFactoryBean alphaJobDetail() { //配置job的名字和分组
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true); //声明这个任务是不是持久保存
        factoryBean.setRequestsRecovery(true);//这个任务是不是可恢复的
        return factoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
    // @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) { //配置job的触发，多久触发一次等等
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000); //多常时间执行一次
        factoryBean.setJobDataMap(new JobDataMap()); //需要存储对象的状态
        return factoryBean;
    }

    // 刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class); //需要定时执行的方法
        factoryBean.setName("postScoreRefreshJob"); //给该方法取名
        factoryBean.setGroup("communityJobGroup");//给该方法分组
        factoryBean.setDurability(true); //声明这个任务是不是持久保存
        factoryBean.setRequestsRecovery(true);//声明这个任务是不是可恢复的
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");//触发器的名字
        factoryBean.setGroup("communityTriggerGroup");//触发器的分组
        factoryBean.setRepeatInterval(1000 * 60 * 5);//间隔多长时间执行一次（5分钟）
        factoryBean.setJobDataMap(new JobDataMap());//需要存储对象的状态
        return factoryBean;
    }

}
