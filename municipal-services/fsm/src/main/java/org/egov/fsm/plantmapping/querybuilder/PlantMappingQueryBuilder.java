package org.egov.fsm.plantmapping.querybuilder;

import java.util.List;

import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.plantmapping.web.model.PlantMappingSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class PlantMappingQueryBuilder {
	

	@Autowired
	private FSMConfiguration config;
	
	private static final String QUERY = "select count(*) OVER() AS full_count, * from eg_fsm_plantmapping plantmapping ";

	private static final String PAGINATION_WRAPPER = "{} {orderby} {pagination}";


	public String getPlantMapSearchQuery(PlantMappingSearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);
		if (criteria.getTenantId() != null) {
			if (criteria.getTenantId().split("\\.").length == 1) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" plantmapping.tenantid like ?");
				preparedStmtList.add('%' + criteria.getTenantId() + '%');
			} else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" plantmapping.tenantid=? ");
				preparedStmtList.add(criteria.getTenantId());
			}
		}
		

		List<String> employeeUuid = criteria.getEmployeeUuid();
		if (!CollectionUtils.isEmpty(employeeUuid)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" plantmapping.employeeuuid IN (").append(createQuery(employeeUuid)).append(")");
			addToPreparedStatement(preparedStmtList, employeeUuid);

		}

		if( criteria.getPlantCode() !=null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" plantmapping.plantcode = ?");
			preparedStmtList.add(criteria.getPlantCode());
		}
		
		List<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" plantmapping.id IN (").append(createQuery(ids)).append(")");
			addToPreparedStatement(preparedStmtList, ids);
		}
		
		return addPaginationWrapper(builder.toString(), preparedStmtList);
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		ids.forEach(id -> {
			preparedStmtList.add(id);
		});

	}

		private Object createQuery(List<String> ids) {
			StringBuilder builder = new StringBuilder();
			int length = ids.size();
			for (int i = 0; i < length; i++) {
				builder.append(" ?");
				if (i != length - 1)
					builder.append(",");
			}
			return builder.toString();
		}


	private void addClauseIfRequired(List<Object> preparedStmtList, StringBuilder builder) {
			if (preparedStmtList.isEmpty())
				builder.append(" WHERE ");
			else {
				builder.append(" AND");
			}
		}
	
	/**
	 * 
	 * @param query
	 *            prepared Query
	 * @param preparedStmtList
	 *            values to be replased on the query
	 * @param criteria
	 *            fsm search criteria
	 * @return the query by replacing the placeholders with preparedStmtList
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList) {
//		
		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = PAGINATION_WRAPPER.replace("{}", query);
		StringBuilder orderQuery = new StringBuilder();
		finalQuery = finalQuery.replace("{orderby}", orderQuery.toString()); 
		
		if (limit == -1) {
			finalQuery = finalQuery.replace("{pagination}", ""); 
		} else {
			finalQuery = finalQuery.replace("{pagination}", " offset ?  limit ?  "); 
			preparedStmtList.add(offset);
			preparedStmtList.add(limit );
		}
		
		return finalQuery;

	}
}
